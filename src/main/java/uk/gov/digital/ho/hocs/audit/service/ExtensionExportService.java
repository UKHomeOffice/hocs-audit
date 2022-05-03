package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExtensionExportService extends DynamicExportService {

    private static final String[] EVENTS = { "EXTENSION_APPLIED" };

    public ExtensionExportService(ObjectMapper objectMapper, AuditRepository auditRepository, InfoClient infoClient,
                                  CaseworkClient caseworkClient, HeaderConverter headerConverter,
                                  MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.EXTENSIONS;
    }

    @Override
    protected String[] parseData(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter) throws JsonProcessingException {
        AuditPayload.Extension extensionData =
                objectMapper.readValue(audit.getAuditPayload(), AuditPayload.Extension.class);

        return new String[]{
                zonedDateTimeConverter.convert(audit.getAuditTimestamp()),
                audit.getType(),
                exportDataConverter.convertValue(audit.getUserID()),
                exportDataConverter.convertCaseUuid(audit.getCaseUUID()),
                Objects.toString(extensionData.getCreated(), ""),
                exportDataConverter.convertValue(extensionData.getType()),
                extensionData.getNote()
        };
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events) {
        LocalDate peggedTo = to.isAfter(LocalDate.now()) ? LocalDate.now() : to;

        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                        from, LocalTime.MIN), LocalDateTime.of(peggedTo, LocalTime.MAX),
                events, caseTypeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public void export(LocalDate from, LocalDate to, OutputStream outputStream, String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var caseTypeDto = getCaseTypeCode(caseType);

        var dataConverter = getDataConverter(convert, caseTypeDto);
        var data = getData(from, to, caseTypeDto.getShortCode(), EVENTS);

        printData(outputStream, zonedDateTimeConverter, dataConverter, convertHeader, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] {
                "timestamp", "event", "userId", "caseId",
                "created", "type", "note"
        };
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = new HashMap<>();

        uuidToName.putAll(infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));
        uuidToName.putAll(infoClient.getCaseTypeActions().stream()
                .collect(Collectors.toMap(action -> action.getUuid().toString(), CaseTypeActionDto::getActionLabel)));

        return new ExportDataConverter(uuidToName, Collections.emptyMap(), caseType.getShortCode(), auditRepository);
    }
}
