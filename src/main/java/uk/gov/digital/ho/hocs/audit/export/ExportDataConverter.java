package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ExportDataConverter {

    private static String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";

    private InfoClient infoClient;
    private CaseworkClient caseworkClient;
    private Map<String, String> uuidToName;

    public ExportDataConverter(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;
    }

    public void initialise() {
        uuidToName = new HashMap<>();

        Set<UserDto> users = infoClient.getUsers();
        users.forEach(user -> uuidToName.put(user.getId(), user.getUsername()));

        Set<TeamDto> teams = infoClient.getTeams();
        teams.forEach(team -> uuidToName.put(team.getUuid().toString(), team.getDisplayName()));

        Set<UnitDto> units = infoClient.getUnits();
        units.forEach(unit -> uuidToName.put(unit.getUuid(), unit.getDisplayName()));

        Set<GetTopicResponse> topics = caseworkClient.getAllCaseTopics();
        topics.forEach(topic -> uuidToName.put(topic.getUuid().toString(), topic.getTopicText()));

        Set<GetCorrespondentOutlineResponse> correspondents = caseworkClient.getAllActiveCorrespondents();
        correspondents.forEach(corr -> uuidToName.put(corr.getUuid().toString(), corr.getFullname()));
    }

    public String[] convertData(String[] auditData) {
        for (int i = 0; i < auditData.length; i++){
            String uuidData = auditData[i];
            if (!isUUID(uuidData)) {
                continue;
            }
            if (uuidToName.containsKey(uuidData)){
                auditData[i] = uuidToName.get(uuidData);
            } else {
                GetCaseReferenceResponse caseReferenceResponse = caseworkClient.getCaseReference(uuidData);
                if (StringUtils.hasText(caseReferenceResponse.getReference())) {
                    uuidToName.put(uuidData, caseReferenceResponse.getReference());
                    auditData[i] = caseReferenceResponse.getReference();
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
}
