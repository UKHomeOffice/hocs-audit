package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeField;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeSchema;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.LogEvent;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.JSON_PARSE_EXCEPTION;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.NON_EXISTENT_VARIABLE;

@Slf4j
@Service
public class SomuExportService {

    private static final String[] EVENTS = {"SOMU_ITEM_UPDATED", "SOMU_ITEM_CREATED"};

    protected final ObjectMapper objectMapper;
    protected final AuditRepository auditRepository;
    protected final InfoClient infoClient;
    private final MalformedDateConverter malformedDateConverter;

    @PersistenceContext
    private EntityManager entityManager;

    public SomuExportService(ObjectMapper objectMapper, AuditRepository auditRepository,
                             InfoClient infoClient, MalformedDateConverter malformedDateConverter) {
        this.objectMapper = objectMapper;
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.malformedDateConverter = malformedDateConverter;
    }

    @Transactional(readOnly = true)
    public void export(LocalDate from, LocalDate to, OutputStream outputStream,
                       String caseType, String somuType, boolean convert,
                       ZonedDateTimeConverter zonedDateTimeConverter)
            throws IOException {
        SomuTypeDto somuTypeDto = infoClient.getSomuType(caseType, somuType);
        Stream<AuditEvent> data = getData(from, to, caseType);
        ExportDataConverter dataConverter = getDataConverter(convert, this.getCaseTypeCode(caseType), getSomuFields(somuTypeDto));

        printData(outputStream, zonedDateTimeConverter, dataConverter, somuTypeDto, data);
    }

    protected void printData(OutputStream outputStream, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter,
                             SomuTypeDto somuType, Stream<AuditEvent> data) throws JsonProcessingException {
        var somuTypeFields =  getSomuFields(somuType);
        String[] headers = getHeaders(somuTypeFields);

        try (
                OutputStream buffer = new BufferedOutputStream(outputStream);
                OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8); var printer =
                        new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                                .setHeader(headers)
                                .setAutoFlush(true)
                                .setNullString("")
                                .build())) {
            data.forEach(audit -> {
                try {
                    if (filterSomuType(audit, somuType)) {
                        String[] parsedData = parseData(audit, somuTypeFields, zonedDateTimeConverter, exportDataConverter);
                        entityManager.detach(audit);

                        parsedData = malformedDateConverter.correctDateFields(parsedData);
                        printer.printRecord((Object[]) parsedData);
                    }
                } catch (IOException e) {
                    throw new AuditExportException("Unable to parse record for audit {} for reason {}", CSV_RECORD_EXPORT_FAILURE, audit.getUuid(), e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("Unable to export record for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
        }
    }

    private String[] parseData(AuditEvent audit, List<SomuTypeField> headers,
                               ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter) throws IOException {
        AuditPayload.SomuItem somuData = objectMapper.readValue(audit.getAuditPayload(), AuditPayload.SomuItem.class);

        List<String> data = new ArrayList<>();
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(exportDataConverter.convertValue(audit.getUserID()));
        data.add(exportDataConverter.convertCaseUuid(audit.getCaseUUID()));
        data.add(somuData.getSomuTypeUuid().toString());
        data.add(somuData.getUuid().toString());

        for (SomuTypeField header : headers) {
            data.add(exportDataConverter.convertValue(somuData.getData().get(header.getName())));
        }
        return data.toArray(new String[0]);
    }

    private ExportDataConverter getDataConverter(boolean convert, String caseType, List<SomuTypeField> somuTypeFields) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = new HashMap<>(infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));

        Map<String, String> entityListItemToName = new HashMap<>();
        somuTypeFields.forEach(field -> {
            var fieldChoices = field.getExtractChoices();
            if (fieldChoices != null) {
                fieldChoices.forEach(choice -> {
                    Set<EntityDto> entities = infoClient.getEntitiesForList(choice);
                    entities.forEach(e -> entityListItemToName.put(e.getSimpleName(), e.getData().getTitle()));
                });
            }
        });

        return new ExportDataConverter(uuidToName, entityListItemToName, caseType, auditRepository);

    }

    private List<SomuTypeField> getSomuFields(SomuTypeDto somuTypeDto) {
        SomuTypeSchema schema;

        try {
            schema = objectMapper.readValue(somuTypeDto.getSchema(), SomuTypeSchema.class);
        } catch (JsonProcessingException exception) {
            log.error("Schema could not be parsed for somu type {}", somuTypeDto.getUuid(), JSON_PARSE_EXCEPTION);
            return Collections.emptyList();
        }

        if (schema.getFields() == null) {
            log.warn("Schema does not exist somu type {}", somuTypeDto.getUuid(), NON_EXISTENT_VARIABLE);
            return Collections.emptyList();
        }

        return schema.getFields();
    }

    private boolean filterSomuType(AuditEvent auditEvent, SomuTypeDto somuTypeDto) throws IOException {
        AuditPayload.SomuItem somuItem = objectMapper.readValue(auditEvent.getAuditPayload(), AuditPayload.SomuItem.class);
        return StringUtils.equals(somuItem.getSomuTypeUuid().toString(), somuTypeDto.getUuid().toString());
    }

    private String getCaseTypeCode(String caseType) {
        return infoClient.getCaseTypes()
                .stream()
                .filter(caseTypeDto -> caseTypeDto.getType().equals(caseType))
                .findFirst()
                .orElseThrow(() -> new AuditExportException("Invalid case type specified %s", LogEvent.INVALID_CASE_TYPE_SPECIFIED, caseType))
                .getShortCode();
    }

    private String[] getHeaders(List<SomuTypeField> somuTypeFields) {
        String[] headers = new String[]{
                "timestamp", "event", "userId", "caseUuid", "somuItemUuid", "somuTypeUuid"
        };

        return Stream.concat(Arrays.stream(headers), somuTypeFields.stream().map(SomuTypeField::getExtractColumnLabel))
                .toArray(String[]::new);
    }

    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseType) {
        LocalDateTime peggedTo = to.isBefore(LocalDate.now()) ? LocalDateTime.of(to, LocalTime.MAX) : LocalDateTime.now();

        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                        from, LocalTime.MIN), peggedTo,
                EVENTS, getCaseTypeCode(caseType));
    }
}
