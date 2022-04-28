package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.EntityDto;
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
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InterestExportService extends DynamicExportService {

    private static final String[] EVENTS = { "EXTERNAL_INTEREST_CREATED", "EXTERNAL_INTEREST_UPDATED" };
    private static final Map<String, String[]> ENTITY_LISTS = Map.of("BF", new String[] {"BF_INTERESTED_PARTIES"},
            "BF2", new String[] {"BF_INTERESTED_PARTIES"},
            "FOI", new String[] {"FOI_INTERESTED_PARTIES"});

    public InterestExportService(ObjectMapper objectMapper, AuditRepository auditRepository, InfoClient infoClient, CaseworkClient caseworkClient, HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.INTERESTS;
    }

    @Override
    protected String[] parseData(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter) throws JsonProcessingException {
        AuditPayload.Interest interestData = objectMapper.readValue(audit.getAuditPayload(), AuditPayload.Interest.class);

        return new String[] {
                zonedDateTimeConverter.convert(audit.getAuditTimestamp()),
                audit.getType(),
                exportDataConverter.convertValue(audit.getUserID()),
                exportDataConverter.convertCaseUuid(audit.getCaseUUID()),
                exportDataConverter.convertValue(interestData.getPartyType()),
                interestData.getInterestDetails()
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
    public void export(LocalDate from, LocalDate to, PrintWriter writer, String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var caseTypeCode = getCaseTypeCode(caseType);

        var dataConverter = getDataConverter(convert, caseTypeCode);
        var data = getData(from, to, caseTypeCode.getShortCode(), EVENTS);

        printData(writer, zonedDateTimeConverter, dataConverter, convertHeader, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] {
                "timestamp", "event", "userId", "caseId", "partyType", "interestDetails"
        };
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername));

        Map<String, String> entityListItemToName = new HashMap<>();

        for (String listName : ENTITY_LISTS.getOrDefault(caseType.getType(), new String[0])) {
            Set<EntityDto> entities = infoClient.getEntitiesForList(listName);
            entities.forEach(e -> entityListItemToName.put(e.getSimpleName(), e.getData().getTitle()));
        }

        return new ExportDataConverter(uuidToName, entityListItemToName, caseType.getShortCode(), auditRepository);
    }
}
