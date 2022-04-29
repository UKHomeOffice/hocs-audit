package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeField;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeSchema;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicTeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.exception.StreamBrokenException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverterFactory;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.parsers.DataParserFactory;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_COMPETE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_START;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public class ExportService {

    private final ObjectMapper mapper;
    private final AuditRepository auditRepository;
    private final InfoClient infoClient;
    private final ExportDataConverterFactory exportDataConverterFactory;
    private final HeaderConverter headerConverter;
    private final MalformedDateConverter malformedDateConverter;
    private final DataParserFactory dataParserFactory;
    public static final String[] CASE_DATA_EVENTS = {"CASE_CREATED", "CASE_UPDATED", "CASE_COMPLETED"};
    public static final String[] CASE_NOTES_EVENTS = {"CASE_NOTE_CREATED", "CASE_NOTE_UPDATED", "CASE_NOTE_DELETED"};
    public static final String[] SOMU_TYPE_EVENTS = {"SOMU_ITEM_UPDATED", "SOMU_ITEM_CREATED"};
    public static final String[] TOPIC_EVENTS = {"CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED"};
    public static final String[] CORRESPONDENT_EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED", "CORRESPONDENT_UPDATED"};
    public static final String[] EXTENSION_EVENTS = {"EXTENSION_APPLIED"};
    public static final String[] ALLOCATION_EVENTS = {"STAGE_ALLOCATED_TO_TEAM", "STAGE_CREATED", "STAGE_RECREATED", "STAGE_COMPLETED", "STAGE_ALLOCATED_TO_USER", "STAGE_UNALLOCATED_FROM_USER"};
    public static final int EXCEL_MAX_CELL_SIZE = 32766;
    public static final String[] APPEAL_EVENTS = {"APPEAL_CREATED", "APPEAL_UPDATED"};
    public static final String[] INTEREST_EVENTS = {"EXTERNAL_INTEREST_CREATED", "EXTERNAL_INTEREST_UPDATED"};


    public ExportService(AuditRepository auditRepository, ObjectMapper mapper, InfoClient infoClient, ExportDataConverterFactory exportDataConverterFactory,
                         HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter, DataParserFactory dataParserFactory) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
        this.infoClient = infoClient;
        this.exportDataConverterFactory = exportDataConverterFactory;
        this.headerConverter = headerConverter;
        this.malformedDateConverter = malformedDateConverter;
        this.dataParserFactory = dataParserFactory;
    }

    @Transactional(readOnly = true)
    public void auditExport(LocalDate from, LocalDate to, OutputStream output, String caseType, ExportType exportType, boolean convert, boolean convertHeader, String timestampFormat, String timeZoneId) throws IOException {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().
                get().getShortCode();
        switch (exportType) {
            case CASE_DATA:
                caseDataExport(from, to, outputWriter, caseTypeCode, caseType, convert, convertHeader, zonedDateTimeConverter);
                break;
            case CASE_NOTES:
                caseNotesExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            case TOPICS:
                topicExport(from, to, outputWriter, caseTypeCode, convertHeader, zonedDateTimeConverter);
                break;
            case CORRESPONDENTS:
                correspondentExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            case EXTENSIONS:
                extensionExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            case ALLOCATIONS:
                allocationExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            case APPEALS:
                appealExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            case INTERESTS:
                interestExport(from, to, outputWriter, caseTypeCode, caseType, convert, convertHeader, zonedDateTimeConverter);
                break;
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    private void printAudit(String[] parsedAudit, OutputStreamWriter outputWriter, CSVPrinter printer) throws StreamBrokenException {
        try {
            if (parsedAudit != null) {
                printer.printRecord(parsedAudit);
                outputWriter.flush();
            }
        } catch (IOException e) {
            throw new StreamBrokenException(e);
        }
    }

    private Stream<AuditEvent> getAuditDataStream(boolean lastAudit, String[] events, LocalDate from, LocalDate to, String caseTypeCode){
        LocalDate peggedTo = to.isAfter(LocalDate.now()) ? LocalDate.now() : to;

        if (lastAudit) {
            if (peggedTo.equals(LocalDate.now())) {
                return auditRepository.findAuditEventLatestEventsAfterDate(LocalDateTime.of(
                        from, LocalTime.MIN), events, caseTypeCode);
            }
            return auditRepository.findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(
                            from, LocalTime.MIN), LocalDateTime.of(peggedTo, LocalTime.MAX),
                    events, caseTypeCode);
        }
        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                from, LocalTime.MIN), LocalDateTime.of(peggedTo, LocalTime.MAX),
                events, caseTypeCode);
    }

    private void genericParseAndPrint(boolean lastAuditDataStream, String[] events, ExportType exportType, LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter, LinkedHashSet<String> caseDataHeaders, List<String> headers) throws IOException {
        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {
            Stream<AuditEvent> data = getAuditDataStream(lastAuditDataStream, events, from, to, caseTypeCode);
            ExportDataConverter exportDataConverter = convert ? exportDataConverterFactory.getInstance() : null;
            data.forEach((audit) -> {
                String[] parsedAudit = null;
                try {
                    parsedAudit = parseData(exportType, audit, zonedDateTimeConverter, caseDataHeaders);
                    if (convert) {
                        parsedAudit = exportDataConverter.convertData(parsedAudit, caseTypeCode);
                    }
                    parsedAudit = malformedDateConverter.correctDateFields(parsedAudit);
                }
                catch(IOException e){
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
                printAudit(parsedAudit, outputWriter, printer);
            });
            log.info("Export {} to CSV Complete", exportType, value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    private String[] parseData(ExportType exportType, AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter, LinkedHashSet<String> caseDataHeaders) throws IOException {
        switch (exportType) {
            case CASE_DATA:
                return parseCaseDataAuditPayload(audit, caseDataHeaders, zonedDateTimeConverter);
            case CASE_NOTES:
                return parseCaseNotesAuditPayload(audit, zonedDateTimeConverter);
            case TOPICS:
                return  parseTopicAuditPayload(audit, zonedDateTimeConverter);
            case CORRESPONDENTS:
                return parseCorrespondentAuditPayload(audit, zonedDateTimeConverter);
            case ALLOCATIONS:
                return parseAllocationAuditPayload(audit, zonedDateTimeConverter);
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    public void caseDataExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CASE_DATA to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "reference", "caseType", "deadline", "primaryCorrespondent", "primaryTopic").collect(Collectors.toList());
        LinkedHashSet<String> caseDataHeaders = infoClient.getCaseExportFields(caseType);
        headers.addAll(caseDataHeaders);
        genericParseAndPrint(true, CASE_DATA_EVENTS, ExportType.CASE_DATA, from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter, caseDataHeaders, headers);
    }

    private String[] parseCaseDataAuditPayload(AuditEvent audit, LinkedHashSet<String> caseDataHeaders, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.CaseData caseData = mapper.readValue(audit.getAuditPayload(), AuditPayload.CaseData.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(caseData.getReference());
        data.add(caseData.getType());
        data.add(Objects.toString(caseData.getCaseDeadline(), ""));
        data.add(Objects.toString(caseData.getPrimaryCorrespondent(), ""));
        data.add(Objects.toString(caseData.getPrimaryTopic(), ""));

        if (caseData.getData() != null) {
            for (String field : caseDataHeaders) {
                data.add(caseData.getData().getOrDefault(field, ""));
            }
        }
        return data.toArray(new String[0]);
    }

    void caseNotesExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CASE_NOTES to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "uuid", "caseNoteType", "text").collect(Collectors.toList());
        genericParseAndPrint(false, CASE_NOTES_EVENTS, ExportType.CASE_NOTES, from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter, null, headers);
    }

    private String[] parseCaseNotesAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.CaseNote caseNote = mapper.readValue(audit.getAuditPayload(), AuditPayload.CaseNote.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(Objects.toString(audit.getUuid()));
        data.add(caseNote.getCaseNoteType());
        data.add(caseNote.getText().substring(0, Math.min(caseNote.getText().length(), EXCEL_MAX_CELL_SIZE - 1)));
        return data.toArray(new String[0]);
    }

    @Transactional(readOnly = true)
    public void auditSomuExport(LocalDate from, LocalDate to, OutputStream output, String caseType, String somuType, boolean convert, String timestampFormat, String timeZoneId) throws IOException {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();

        log.info("Exporting Case Data Somu to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "somuItemUuid", "somuTypeUuid").collect(Collectors.toList());
        SomuTypeDto somuTypeDto = infoClient.getSomuType(caseType, somuType);
        SomuTypeSchema schema = mapper.readValue(somuTypeDto.getSchema(), SomuTypeSchema.class);
        LinkedHashSet<SomuTypeField> somuFields = new LinkedHashSet<>(schema.getFields());

        headers.addAll(somuFields.stream().map(SomuTypeField::getExtractColumnLabel).collect(Collectors.toList()));

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {
            Stream<AuditEvent> data = getAuditDataStream(false, SOMU_TYPE_EVENTS, from, to, caseTypeCode);
            ExportDataConverter exportDataConverter = convert ? exportDataConverterFactory.getInstance() : null;
            data.forEach((audit) -> {
                String[] parsedAudit = null;
                try {
                    if (filterSomuIType(audit, somuTypeDto)) {
                        parsedAudit = parseCaseDataSomuAuditPayload(audit, somuFields, zonedDateTimeConverter);
                        if (convert) {
                            parsedAudit = exportDataConverter.convertData(parsedAudit, caseTypeCode);
                        }
                        parsedAudit = malformedDateConverter.correctDateFields(parsedAudit);
                    }
                } catch (Exception e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
                printAudit(parsedAudit, outputWriter, printer);
            });
            log.info("Export SOMU_DATA to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    private String[] parseCaseDataSomuAuditPayload(AuditEvent audit, Set<SomuTypeField> headers, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.SomuItem somuData = mapper.readValue(audit.getAuditPayload(), AuditPayload.SomuItem.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(somuData.getSomuTypeUuid().toString());
        data.add(somuData.getUuid().toString());

        for (SomuTypeField header : headers) {
            data.add(getSomuDataValue(somuData.getData(), header.getName()));
        }
        return data.toArray(new String[0]);
    }

    private boolean filterSomuIType(AuditEvent auditEvent, SomuTypeDto somuTypeDto) throws IOException {
        AuditPayload.SomuItem somuItem = mapper.readValue(auditEvent.getAuditPayload(), AuditPayload.SomuItem.class);
        return StringUtils.equals(somuItem.getSomuTypeUuid().toString(), somuTypeDto.getUuid().toString());
    }

    private String getSomuDataValue(Map<String,String> data, String key) {
        if (data != null) {
            return data.get(key);
        }
        return "";
    }

    public void topicExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting TOPIC to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "topicUuid", "topic").collect(Collectors.toList());
        List<String> substitutedHeaders = headers;

        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {
            Stream<AuditEvent> data = getAuditDataStream(false, TOPIC_EVENTS, from, to, caseTypeCode);

            data.forEach((audit) -> {
                String[] parsedAudit = null;
                try {
                    parsedAudit = parseTopicAuditPayload(audit, zonedDateTimeConverter);
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
                printAudit(parsedAudit, outputWriter, printer);
            });
            log.info("Export TOPIC to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    private String[] parseTopicAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();

        AuditPayload.Topic topicData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Topic.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(topicData.getTopicUuid().toString());
        data.add(topicData.getTopicName());
        return data.toArray(new String[0]);
    }

    public void correspondentExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CORRESPONDENT to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid",
                "correspondentUuid", "fullname", "organisation", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference", "externalKey").collect(Collectors.toList());
        genericParseAndPrint(false, CORRESPONDENT_EVENTS, ExportType.CORRESPONDENTS, from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter, null, headers);
    }

    public void extensionExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting EXTENSION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseId",
                "created", "type", "note").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditEvent> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    EXTENSION_EVENTS, caseTypeCode);

            ExportDataConverter exportDataConverter = convert ? exportDataConverterFactory.getInstance() : null;

            data.forEach((audit) -> {
                try {
                    String[] auditRow = parseExtensionAuditPayload(audit, zonedDateTimeConverter);
                    if (convert){
                        auditRow = exportDataConverter.convertData(auditRow, caseTypeCode);
                    }
                    auditRow = malformedDateConverter.correctDateFields(auditRow);
                    printer.printRecord(auditRow);
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to get record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export EXTENSION to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseExtensionAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Extension extensionData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Extension.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(Objects.toString(extensionData.getCreated(), null));
        data.add(extensionData.getType());
        data.add(extensionData.getNote());

        return data.toArray(new String[data.size()]);
    }

    private String[] parseCorrespondentAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Correspondent correspondentData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Correspondent.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(correspondentData.getUuid().toString());
        data.add(correspondentData.getFullname());
        data.add(correspondentData.getOrganisation());

        if (correspondentData.getAddress() != null) {
            data.add(correspondentData.getAddress().getAddress1());
            data.add(correspondentData.getAddress().getAddress2());
            data.add(correspondentData.getAddress().getAddress3());
            data.add(correspondentData.getAddress().getCountry());
            data.add(correspondentData.getAddress().getPostcode());
        } else {
            data.addAll(Arrays.asList("", "", "", "", ""));
        }

        data.add(correspondentData.getTelephone());
        data.add(correspondentData.getEmail());
        data.add(correspondentData.getReference());
        data.add(correspondentData.getExternalKey());
        return data.toArray(new String[0]);
    }

    public void allocationExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting ALLOCATION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "stage", "allocatedTo", "deadline").collect(Collectors.toList());
        genericParseAndPrint(false, ALLOCATION_EVENTS, ExportType.ALLOCATIONS, from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter, null, headers);
    }

    private String[] parseAllocationAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();

        AuditPayload.StageAllocation allocationData = mapper.readValue(audit.getAuditPayload(), AuditPayload.StageAllocation.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(allocationData.getStage());
        data.add(Objects.toString(allocationData.getAllocatedToUUID(), ""));
        data.add(Objects.toString(allocationData.getDeadline(), ""));

        return data.toArray(new String[0]);
    }

    public void staticTopicExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TOPIC LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        List<String> headers = Stream.of("topicUUID", "topicName", "active").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TopicDto> topics = infoClient.getTopics();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {

            topics.forEach((topic) -> {
                try {
                    printer.printRecord(topic.getValue(), topic.getLabel(), topic.isActive());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static topic {} for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                    throw new StreamBrokenException(e);
                }
            });
            log.info("Export STATIC TOPIC LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }

    }

    public void staticTopicsWithTeamsExport(OutputStream output,
                                            String caseType, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TOPICS with TEAMS LIST to CSV", value(EVENT, CSV_EXPORT_START));
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        List<String> headers = Stream.of("caseType", "topicUUID", "topicName", "teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TopicTeamDto> topicTeams = infoClient.getTopicsWithTeams(caseType);

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))){

            topicTeams.forEach(topic -> {
                topic.getTeams().forEach(team -> {
                    try {
                        printer.printRecord(caseType, topic.getUuid(), topic.getDisplayName(), team.getUuid(), team.getDisplayName());
                        outputWriter.flush();
                    } catch (IOException e) {
                        log.error("Unable to parse record for static topic {} and team {} for reason {}", topic.getUuid(), team.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                        throw new StreamBrokenException(e);
                    }
                });
            });
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    public void staticUserExport(OutputStream output, boolean convertHeader) throws IOException {

        log.info("Exporting STATIC USER LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        List<String> headers = Stream.of("userUUID", "username", "firstName", "lastName", "email").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<UserDto> users = infoClient.getUsers();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {

            users.forEach((user) -> {

                try {
                    printer.printRecord(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static user {} for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                    throw new StreamBrokenException(e);
                }
            });
            log.info("Export STATIC USER LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    public void staticTeamExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TEAM LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        List<String> headers = Stream.of("teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TeamDto> teams = infoClient.getTeams();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {

            teams.forEach((team) -> {

                try {
                    printer.printRecord(team.getUuid(), team.getDisplayName());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static team {} for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                    throw new StreamBrokenException(e);
                }
            });
            log.info("Export STATIC TEAM LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    public void staticUnitsForTeamsExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC UNITS and TEAMS LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
        List<String> headers = Stream.of("unitUUID", "unitName", "teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<UnitDto> units = infoClient.getUnits();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {

            units.forEach((unit) -> {
                Set<TeamDto> teams = infoClient.getTeamsForUnit(unit.getUuid());

                if (teams.size() == 0) {
                    try {
                        printer.printRecord(unit.getUuid(), unit.getDisplayName(), null, null);
                        outputWriter.flush();
                    } catch (IOException e) {
                        log.error("Unable to parse record for static unit {} for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                        throw new StreamBrokenException(e);
                    }
                } else {
                    teams.forEach((team) -> {
                        try {
                            printer.printRecord(unit.getUuid(), unit.getDisplayName(), team.getUuid(), team.getDisplayName());
                            outputWriter.flush();
                        } catch (IOException e) {
                            log.error("Unable to parse record for static unit and team {} for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                            throw new StreamBrokenException(e);
                        }
                    });
                }
            });
            log.info("Export STATIC UNITS and TEAMS LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
        catch (StreamBrokenException e) {
            outputWriter.close();
            log.error("Unable to export record for reason {}", e.getThrowable(), value(EVENT, CSV_EXPORT_FAILURE));
            throw new IOException(e);
        }
    }

    private void appealExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode,
                              boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {

        log.info("Exporting APPEALS to CSV", value(EVENT, CSV_EXPORT_START));

        List<String> headers = Stream.of("timestamp", "event", "userId", "caseId",
                "created", "type", "status", "dateSentRMS", "outcome", "complex",
                "note", "officerType", "officerName","officerDirectorate").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditEvent> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                            from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    APPEAL_EVENTS, caseTypeCode);

            ExportDataConverter exportDataConverter = convert ? exportDataConverterFactory.getInstance() : null;

            data.forEach((audit) -> {
                try {
                    String[] auditRow = parseAppealAuditPayload(audit, zonedDateTimeConverter);
                    if (convert){
                        auditRow = exportDataConverter.convertData(auditRow, caseTypeCode);
                    }
                    auditRow = malformedDateConverter.correctDateFields(auditRow);
                    printer.printRecord(auditRow);
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to get record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export APPEALS to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseAppealAuditPayload(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Appeal appealData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Appeal.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(Objects.toString(appealData.getCreated(), null));
        data.add(Objects.toString(appealData.getType(), null));
        data.add(appealData.getStatus());
        data.add(Objects.toString(appealData.getDateSentRMS(), null));
        data.add(appealData.getOutcome());
        data.add(appealData.getComplexCase());
        data.add(appealData.getNote());
        data.add(appealData.getOfficerType());
        data.add(appealData.getOfficerName());
        data.add(appealData.getOfficerDirectorate());

        return data.toArray(new String[data.size()]);
    }

    private void interestExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode,
                              String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting INTERESTS to CSV", value(EVENT, CSV_EXPORT_START));

        List<String> headers = List.of("timestamp", "event", "userId", "caseId", "partyType", "interestDetails");

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[0])))) {
            Stream<AuditEvent> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                            from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    INTEREST_EVENTS, caseTypeCode);

            var dataParser = dataParserFactory.getInterestInstance(caseType, convert, zonedDateTimeConverter);

            data.forEach(audit -> {
                try {
                    printer.printRecord((Object[])dataParser.parsePayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to get record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export INTERESTS to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }


}