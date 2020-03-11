package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.adapter.AbstractExportViewFieldAdapter;
import uk.gov.digital.ho.hocs.audit.export.adapter.UserEmailAdapter;
import uk.gov.digital.ho.hocs.audit.export.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
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

    private ObjectMapper mapper;
    private AuditRepository auditRepository;
    private InfoClient infoClient;
    public static final String[] CASE_DATA_EVENTS = {"CASE_CREATED", "CASE_UPDATED"};
    public static final String[] TOPIC_EVENTS = {"CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED"};
    public static final String[] CORRESPONDENT_EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED", "CORRESPONDENT_UPDATED"};
    public static final String[] ALLOCATION_EVENTS = {"STAGE_ALLOCATED_TO_TEAM", "STAGE_CREATED", "STAGE_RECREATED", "STAGE_COMPLETED", "STAGE_ALLOCATED_TO_USER", "STAGE_UNALLOCATED_FROM_USER"};

    public ExportService(AuditRepository auditRepository, ObjectMapper mapper, InfoClient infoClient) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
        this.infoClient = infoClient;
    }

    @Transactional(readOnly = true)
    public void auditExport(LocalDate from, LocalDate to, OutputStream output, String caseType, ExportType exportType) throws IOException {
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();
        switch (exportType) {
            case CASE_DATA:
                caseDataExport(from, to, outputWriter, caseTypeCode, caseType);
                break;
            case TOPICS:
                topicExport(from, to, outputWriter, caseTypeCode);
                break;
            case CORRESPONDENTS:
                correspondentExport(from, to, outputWriter, caseTypeCode);
                break;
            case ALLOCATIONS:
                allocationExport(from, to, outputWriter, caseTypeCode);
                break;
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    void caseDataExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, String caseType) throws IOException {
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
                    printer.printRecord(parseCaseDataAuditPayload(audit, caseDataHeaders));
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

    void correspondentExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
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
                    printer.printRecord(parseCorrespondentAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CORRESPONDENT to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseCorrespondentAuditPayload(AuditData audit) throws IOException {
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
        return data;
    }

    void allocationExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
        log.info("Exporting ALLOCATION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event", "userId", "caseUuid", "stage", "allocatedTo", "deadline").collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    ALLOCATION_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseAllocationAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export ALLOCATION to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseAllocationAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.StageAllocation allocationData = mapper.readValue(audit.getAuditPayload(), AuditPayload.StageAllocation.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(allocationData.getStage());
        data.add(Objects.toString(allocationData.getAllocatedToUUID(), ""));
        data.add(Objects.toString(allocationData.getDeadline(), ""));
        return data;
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


    @Transactional(readOnly = true)
    public void customExport(LocalDate from, LocalDate to, OutputStream output, ExportViewDto exportViewDto) throws IOException {
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");

        List<String> headers = buildHeaders(exportViewDto);
        List<Object[]> dataList = auditRepository.getResultsFromView(exportViewDto.getCode());


        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

            dataList.forEach((dataRow) -> {
                try {
                    printer.printRecord(convertCustomData(exportViewDto.getFields(), dataRow));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for custom report, reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export Custom Report '{}' to CSV Complete, event {}", exportViewDto.getCode(), value(EVENT, CSV_EXPORT_COMPETE));
        }

    }

    private List<String> buildHeaders(ExportViewDto exportViewDto) {
        List<String> headers = new ArrayList<>();

        for (ExportViewFieldDto viewFieldDto : exportViewDto.getFields()) {
            if (!shouldHide(viewFieldDto)) {
                headers.add(viewFieldDto.getDisplayName());
            }
        }

        return headers;
    }

    private boolean shouldHide(ExportViewFieldDto viewFieldDto) {

        for (ExportViewFieldAdapterDto adapterDto : viewFieldDto.getAdapters()) {
            if (ExportViewConstants.FIELD_ADAPTER_HIDDEN.equals(adapterDto.getType())) {
                return true;
            }
        }

        return false;
    }

    private List<String> convertCustomData(List<ExportViewFieldDto> fields, Object[] rawData) {
        List<String> results = new ArrayList<>();
        Map<String, AbstractExportViewFieldAdapter> adapters = getAdapters();
        int index = 0;
        for (ExportViewFieldDto fieldDto : fields) {
            if (!shouldHide(fieldDto)) {
                results.add(applyAdapters(rawData[index], fieldDto.getAdapters(), adapters));
            }
            index++;
        }


        return results;
    }

    private String applyAdapters(Object data, List<ExportViewFieldAdapterDto> adaptersDtos, Map<String, AbstractExportViewFieldAdapter> adapters) {


        Object result = data;
        for (ExportViewFieldAdapterDto adapterDto : adaptersDtos) {
            AbstractExportViewFieldAdapter adapterToUse = adapters.get(adapterDto.getType());

            if (adapterToUse != null) {
                result = adapterToUse.convert(result);
            } else {
                throw new IllegalArgumentException("Cannot convert data for Adapter Type: " + adapterDto.getType());
            }
        }

        return result == null ? null : String.valueOf(result);

    }

    private Map<String, AbstractExportViewFieldAdapter> getAdapters() {
        Map<String, AbstractExportViewFieldAdapter> result = new HashMap<>();
        result.put(ExportViewConstants.FIELD_ADAPTER_USER_EMAIL, new UserEmailAdapter(infoClient));
        result.put(ExportViewConstants.FIELD_ADAPTER_USER_EMAIL, new UserEmailAdapter(infoClient));

        return result;

    }


}


