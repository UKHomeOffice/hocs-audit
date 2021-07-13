package uk.gov.digital.ho.hocs.audit.export;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ExportDataConverter {

    private static final String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private static final String[] MPAM_CODE_MAPPING_LISTS = { "MPAM_ENQUIRY_SUBJECTS", "MPAM_ENQUIRY_REASONS_ALL", "MPAM_BUS_UNITS_ALL" };
    private static final Set<String> MPAM_SHORT_CODES = Set.of("b5", "b6");

    private final InfoClient infoClient;
    private final CaseworkClient caseworkClient;
    private final Map<String, String> uuidToName;
    private final Map<String, String> mpamCodeToName;

    public ExportDataConverter(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;

        uuidToName = new ConcurrentHashMap<>();
        mpamCodeToName = new ConcurrentHashMap<>();
    }

    public void initialise() {
        Set<UserDto> users = infoClient.getUsers();
        users.forEach(user -> uuidToName.put(user.getId(), user.getUsername()));

        Set<TeamDto> teams = infoClient.getAllTeams();
        teams.forEach(team -> uuidToName.put(team.getUuid().toString(), team.getDisplayName()));

        Set<UnitDto> units = infoClient.getUnits();
        units.forEach(unit -> uuidToName.put(unit.getUuid(), unit.getDisplayName()));

        Set<GetTopicResponse> topics = caseworkClient.getAllCaseTopics();
        topics.forEach(topic -> uuidToName.put(topic.getUuid().toString(), topic.getTopicText()));

        Set<GetCorrespondentOutlineResponse> correspondents = caseworkClient.getAllActiveCorrespondents();
        correspondents.forEach(corr -> uuidToName.put(corr.getUuid().toString(), corr.getFullname()));

        for (String listName : MPAM_CODE_MAPPING_LISTS) {
            Set<EntityDto> entities = infoClient.getEntitiesForList(listName);
            entities.forEach(e -> mpamCodeToName.put(e.getSimpleName(), e.getData().getTitle()));
        }
    }

    public String[] convertData(String[] auditData, String caseShortCode) {
        for (int i = 0; i < auditData.length; i++){
            String fieldData = auditData[i];
            if (isUUID(fieldData)) {
                if (uuidToName.containsKey(fieldData)) {
                    auditData[i] = uuidToName.get(fieldData);
                } else {
                    GetCaseReferenceResponse caseReferenceResponse = caseworkClient.getCaseReference(fieldData);
                    if (StringUtils.hasText(caseReferenceResponse.getReference())) {
                        uuidToName.put(fieldData, caseReferenceResponse.getReference());
                        auditData[i] = caseReferenceResponse.getReference();
                    }
                }
            } else {
                if (MPAM_SHORT_CODES.contains(caseShortCode)) {
                    if (mpamCodeToName.containsKey(fieldData)) {
                        String displayValue = mpamCodeToName.get(fieldData);
                        String sanitizedDisplayValue = sanitiseForCsv(displayValue);
                        auditData[i] = sanitizedDisplayValue;
                    }
                }
            }
        }
        return auditData;
    }

    boolean isUUID(String uuid) {
        if (StringUtils.hasText(uuid)) {
            return uuid.matches(UUID_REGEX);
        }
        return false;
    }

    private String sanitiseForCsv(String value) {
        return value.replace(",", "");
    }
}
