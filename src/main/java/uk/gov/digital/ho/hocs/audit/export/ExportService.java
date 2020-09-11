package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

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

    private ObjectMapper mapper;
    private AuditRepository auditRepository;
    private InfoClient infoClient;
    private ExportDataConverter exportDataConverter;
    public static final String[] CASE_DATA_EVENTS = {"CASE_CREATED", "CASE_UPDATED"};
    public static final String[] TOPIC_EVENTS = {"CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED"};
    public static final String[] CORRESPONDENT_EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED", "CORRESPONDENT_UPDATED"};
    public static final String[] ALLOCATION_EVENTS = {"STAGE_ALLOCATED_TO_TEAM", "STAGE_CREATED", "STAGE_RECREATED", "STAGE_COMPLETED", "STAGE_ALLOCATED_TO_USER", "STAGE_UNALLOCATED_FROM_USER"};

    public ExportService(AuditRepository auditRepository, ObjectMapper mapper, InfoClient infoClient, ExportDataConverter exportDataConverter) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
        this.infoClient = infoClient;
        this.exportDataConverter = exportDataConverter;
    }

    @Transactional(readOnly = true)
    public void auditExport(LocalDate from, LocalDate to, OutputStream output, String caseType, ExportType exportType, boolean convert) throws IOException {
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();
        switch (exportType) {
            case CASE_DATA:
                caseDataExport(from, to, outputWriter, caseTypeCode, caseType, convert);
                break;
            case TOPICS:
                topicExport(from, to, outputWriter, caseTypeCode);
                break;
            case CORRESPONDENTS:
                correspondentExport(from, to, outputWriter, caseTypeCode, convert);
                break;
            case ALLOCATIONS:
                allocationExport(from, to, outputWriter, caseTypeCode, convert);
                break;
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    void caseDataExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, String caseType, boolean convert) throws IOException {
        log.info("Exporting CASE_DATA to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "reference", "caseType", "deadline", "primaryCorrespondent", "primaryTopic").collect(Collectors.toList());
        LinkedHashSet<String> caseDataHeaders = infoClient.getCaseExportFields(caseType);
        headers.addAll(caseDataHeaders);


        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CASE_DATA_EVENTS, caseTypeCode);

            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseCaseDataAuditPayload(audit, caseDataHeaders);
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

    private String[] parseCaseDataAuditPayload(AuditData audit, LinkedHashSet<String> caseDataHeaders) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.CaseData caseData = mapper.readValue(audit.getAuditPayload(), AuditPayload.CaseData.class);
        data.add(audit.getAuditTimestamp().toString());
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

    void topicExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
        log.info("Exporting TOPIC to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "topicUuid", "topic").collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    TOPIC_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseTopicAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export TOPIC to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseTopicAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Topic topicData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Topic.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(topicData.getTopicUuid().toString());
        data.add(topicData.getTopicName());
        return data;
    }

    void correspondentExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert) throws IOException {
        log.info("Exporting CORRESPONDENT to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid",
                "correspondentUuid", "fullname", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference", "externalKey").collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CORRESPONDENT_EVENTS, caseTypeCode);

            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseCorrespondentAuditPayload(audit);
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

    private String[] parseCorrespondentAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Correspondent correspondentData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Correspondent.class);
        data.add(audit.getAuditTimestamp().toString());
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

    void allocationExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, boolean convert) throws IOException {
        log.info("Exporting ALLOCATION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "stage", "allocatedTo", "deadline").collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    ALLOCATION_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    String[] parsedAudit = parseAllocationAuditPayload(audit);
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

    private String[] parseAllocationAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.StageAllocation allocationData = mapper.readValue(audit.getAuditPayload(), AuditPayload.StageAllocation.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(allocationData.getStage());
        data.add(Objects.toString(allocationData.getAllocatedToUUID(), ""));
        data.add(Objects.toString(allocationData.getDeadline(), ""));
        return data.toArray(new String[data.size()]);
    }

    public void staticTopicExport(OutputStream output) throws IOException {
        log.info("Exporting STATIC TOPIC LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("topicUUID", "topicName").collect(Collectors.toList());

        Set<TopicDto> topics = infoClient.getTopics();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

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

    public void staticUserExport(OutputStream output) throws IOException {

        log.info("Exporting STATIC USER LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("userUUID", "username", "firstName", "lastName", "email").collect(Collectors.toList());

        Set<UserDto> users = infoClient.getUsers();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

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

    public void staticTeamExport(OutputStream output) throws IOException {
        log.info("Exporting STATIC TEAM LIST to CSV", value(EVENT, CSV_EXPORT_START));

        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        List<String> headers = Stream.of("teamUUID", "teamName").collect(Collectors.toList());

        Set<TeamDto> teams = infoClient.getTeams();

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

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

}


