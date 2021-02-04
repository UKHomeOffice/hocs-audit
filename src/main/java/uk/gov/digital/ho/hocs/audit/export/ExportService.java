package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Service
public class ExportService {

    private final ObjectMapper mapper;
    private final AuditRepository auditRepository;
    private final InfoClient infoClient;
    private final ExportDataConverter exportDataConverter;
    private final HeaderConverter headerConverter;
    public static final String[] CASE_DATA_EVENTS = {"CASE_CREATED", "CASE_UPDATED", "CASE_COMPLETED"};
    public static final String[] CASE_NOTES_EVENTS = {"CASE_NOTE_CREATED", "CASE_NOTE_UPDATED", "CASE_NOTE_DELETED"};
    public static final String[] SOMU_TYPE_EVENTS = {"SOMU_ITEM_UPDATED", "SOMU_ITEM_CREATED"};
    public static final String[] TOPIC_EVENTS = {"CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED"};
    public static final String[] CORRESPONDENT_EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED", "CORRESPONDENT_UPDATED"};
    public static final String[] ALLOCATION_EVENTS = {"STAGE_ALLOCATED_TO_TEAM", "STAGE_CREATED", "STAGE_RECREATED", "STAGE_COMPLETED", "STAGE_ALLOCATED_TO_USER", "STAGE_UNALLOCATED_FROM_USER"};

    public ExportService(AuditRepository auditRepository, ObjectMapper mapper, InfoClient infoClient, ExportDataConverter exportDataConverter,
                         HeaderConverter headerConverter) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
        this.infoClient = infoClient;
        this.exportDataConverter = exportDataConverter;
        this.headerConverter = headerConverter;
    }

    @Transactional(readOnly = true)
    public void auditExport(LocalDate from, LocalDate to, OutputStream output, String caseType, ExportType exportType, boolean convert, boolean convertHeader, final String timestampFormat, final String timeZoneId) throws IOException {
        final ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();
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
            case ALLOCATIONS:
                allocationExport(from, to, outputWriter, caseTypeCode, convert, convertHeader, zonedDateTimeConverter);
                break;
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    void caseDataExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, String caseType, boolean convert, boolean convertHeader, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CASE_DATA to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "reference", "caseType", "deadline", "primaryCorrespondent", "primaryTopic").collect(Collectors.toList());
        LinkedHashSet<String> caseDataHeaders = infoClient.getCaseExportFields(caseType);
        headers.addAll(caseDataHeaders);

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        if (convert){
            exportDataConverter.initialise();
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditData> data = auditRepository.findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CASE_DATA_EVENTS, caseTypeCode);

            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseCaseDataAuditPayload(audit, caseDataHeaders, zonedDateTimeConverter);
                    if (convert){
                        parsedAudit = exportDataConverter.convertData(parsedAudit);
                    }
                    printer.printRecord(parsedAudit);
                    outputWriter.flush();
                } catch (Exception e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CASE_DATA to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseCaseDataAuditPayload(AuditData audit, LinkedHashSet<String> caseDataHeaders, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
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
        return data.toArray(new String[data.size()]);
    }

    void caseNotesExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CASE_NOTES to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "uuid", "caseNoteType", "text").collect(Collectors.toList());
        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        if (convert){
            exportDataConverter.initialise();
        }
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CASE_NOTES_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseCaseNotesAuditPayload(audit, zonedDateTimeConverter);
                    if (convert){
                        parsedAudit = exportDataConverter.convertData(parsedAudit);
                    }
                    printer.printRecord(parsedAudit);
                    outputWriter.flush();
                } catch (Exception e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CASE_NOTES to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseCaseNotesAuditPayload(AuditData audit, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.CaseNote caseNote = mapper.readValue(audit.getAuditPayload(), AuditPayload.CaseNote.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(Objects.toString(audit.getUuid()));
        data.add(caseNote.getCaseNoteType());
        data.add(caseNote.getText());
        return data.toArray(new String[data.size()]);
    }

    @Transactional(readOnly = true)
    public void auditSomuExport(LocalDate from, LocalDate to, OutputStream output, String caseType, String somuType, boolean convert, final String timestampFormat, final String timeZoneId) throws IOException {
        final ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();

        log.info("Exporting Case Data Somu to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "somuTypeUuid", "somuItemUuid").collect(Collectors.toList());
        SomuTypeDto somuTypeDto = infoClient.getSomuType(caseType, somuType);
        SomuTypeSchema schema = mapper.readValue(somuTypeDto.getSchema(), SomuTypeSchema.class);
        LinkedHashSet<String> somuHeaders = new LinkedHashSet<>();
        for (SomuTypeField field : schema.getFields()) {
            somuHeaders.add(field.getName());
        }
        headers.addAll(somuHeaders);

        if (convert){
            exportDataConverter.initialise();
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(
                    LocalDateTime.of(from, LocalTime.MIN),
                    LocalDateTime.of(to, LocalTime.MAX),
                    SOMU_TYPE_EVENTS,
                    caseTypeCode);

            data.forEach((audit) -> {
                try {
                    if (filterSomuIType(audit, somuTypeDto)) {
                        String[] parsedAudit = parseCaseDataSomuAuditPayload(audit, somuHeaders, zonedDateTimeConverter);
                        if (convert) {
                            parsedAudit = exportDataConverter.convertData(parsedAudit);
                        }
                        printer.printRecord(parsedAudit);
                        outputWriter.flush();
                    }
                } catch (Exception e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CASE_DATA to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseCaseDataSomuAuditPayload(AuditData audit, Set<String> headers, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.SomuItem somuData = mapper.readValue(audit.getAuditPayload(), AuditPayload.SomuItem.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(somuData.getUuid().toString());
        data.add(somuData.getSomuTypeUuid().toString());

        for (String header : headers) {
            data.add(getSomuDataValue(somuData.getData(), header));
        }
        return data.toArray(new String[data.size()]);
    }

    private boolean filterSomuIType(AuditData auditData, SomuTypeDto somuTypeDto) throws IOException {
        AuditPayload.SomuItem somuItem = mapper.readValue(auditData.getAuditPayload(), AuditPayload.SomuItem.class);
        return StringUtils.equals(somuItem.getSomuTypeUuid().toString(), somuTypeDto.getUuid().toString());
    }

    private String getSomuDataValue(Map<String,String> data, String key) {
        if (data != null) {
            return data.get(key);
        }
        return "";
    }

    void topicExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convertHeader, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting TOPIC to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "topicUuid", "topic").collect(Collectors.toList());
        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    TOPIC_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseTopicAuditPayload(audit, zonedDateTimeConverter));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export TOPIC to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseTopicAuditPayload(AuditData audit, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();

        AuditPayload.Topic topicData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Topic.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(topicData.getTopicUuid().toString());
        data.add(topicData.getTopicName());
        return data;
    }

    void correspondentExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting CORRESPONDENT to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid",
                "correspondentUuid", "fullname", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference", "externalKey").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        if (convert){
            exportDataConverter.initialise();
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CORRESPONDENT_EVENTS, caseTypeCode);

            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseCorrespondentAuditPayload(audit, zonedDateTimeConverter);
                    if (convert){
                        parsedAudit = exportDataConverter.convertData(parsedAudit);
                    }
                    printer.printRecord(parsedAudit);
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CORRESPONDENT to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseCorrespondentAuditPayload(AuditData audit, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Correspondent correspondentData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Correspondent.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(correspondentData.getUuid().toString());
        data.add(correspondentData.getFullname());

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
        return data.toArray(new String[data.size()]);
    }

    void allocationExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert, boolean convertHeader, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        log.info("Exporting ALLOCATION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "stage", "allocatedTo", "deadline").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        if (convert){
            exportDataConverter.initialise();
        }

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    ALLOCATION_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseAllocationAuditPayload(audit, zonedDateTimeConverter);
                    if (convert){
                        parsedAudit = exportDataConverter.convertData(parsedAudit);
                    }
                    printer.printRecord(parsedAudit);
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export ALLOCATION to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseAllocationAuditPayload(AuditData audit, final ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        List<String> data = new ArrayList<>();

        AuditPayload.StageAllocation allocationData = mapper.readValue(audit.getAuditPayload(), AuditPayload.StageAllocation.class);
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(allocationData.getStage());
        data.add(Objects.toString(allocationData.getAllocatedToUUID(), ""));
        data.add(Objects.toString(allocationData.getDeadline(), ""));

        return data.toArray(new String[data.size()]);
    }

    public void staticTopicExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TOPIC LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("topicUUID", "topicName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TopicDto> topics = infoClient.getTopics();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

            topics.forEach((topic) -> {
                try {
                    printer.printRecord(topic.getValue(), topic.getLabel());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static topic {} for reason {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export STATIC TOPIC LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }

    }

    public void staticTopicsWithTeamsExport(OutputStream output,
                                            String caseType, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TOPICS with TEAMS LIST to CSV", value(EVENT, CSV_EXPORT_START));
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("caseType", "topicUUID", "topicName", "teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TopicTeamDto> topicTeams = infoClient.getTopicsWithTeams(caseType);

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))){

            topicTeams.forEach(topic -> {

                topic.getTeams().forEach(team -> {
                    try {
                        printer.printRecord(caseType, topic.getUuid(), topic.getDisplayName(), team.getUuid(), team.getDisplayName());
                        outputWriter.flush();
                    } catch (IOException e) {
                        log.error("Unable to parse record for static topic {} and team {} for reason {}", topic.getUuid(), team.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                    }
                });
            });
        }
    }

    public void staticUserExport(OutputStream output, boolean convertHeader) throws IOException {

        log.info("Exporting STATIC USER LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("userUUID", "username", "firstName", "lastName", "email").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<UserDto> users = infoClient.getUsers();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

            users.forEach((user) -> {
                try {
                    printer.printRecord(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static user {} for reason {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export STATIC USER LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    public void staticTeamExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC TEAM LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<TeamDto> teams = infoClient.getTeams();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

            teams.forEach((team) -> {
                try {
                    printer.printRecord(team.getUuid(), team.getDisplayName());
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for static team {} for reason {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export STATIC TEAM LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    public void staticUnitsForTeamsExport(OutputStream output, boolean convertHeader) throws IOException {
        log.info("Exporting STATIC UNITS and TEAMS LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("unitUUID", "unitName", "teamUUID", "teamName").collect(Collectors.toList());

        List<String> substitutedHeaders = headers;
        if (convertHeader) {
            substitutedHeaders = headerConverter.substitute(headers);
        }
        Set<UnitDto> units = infoClient.getUnits();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

            units.forEach((unit) -> {
                Set<TeamDto> teams = infoClient.getTeamsForUnit(unit.getUuid());

                if (teams.size() == 0) {
                    try {
                        printer.printRecord(unit.getUuid(), unit.getDisplayName(), null, null);
                        outputWriter.flush();
                    } catch (IOException e) {
                        log.error("Unable to parse record for static unit {} for reason {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                    }
                } else {
                    teams.forEach((team) -> {
                        try {
                            printer.printRecord(unit.getUuid(), unit.getDisplayName(), team.getUuid(), team.getDisplayName());
                            outputWriter.flush();
                        } catch (IOException e) {
                            log.error("Unable to parse record for static unit and team {} for reason {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                        }
                    });
                }
            });
            log.info("Export STATIC UNITS and TEAMS LIST to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }
}


