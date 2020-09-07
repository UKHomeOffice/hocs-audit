package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.*;

@Slf4j
@Service
public class ExportDataConverter {

    private InfoClient infoClient;
    private CaseworkClient caseworkClient;
    private Map<String, String> uuidToName;

    public ExportDataConverter(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;
    }

    public String[] convertData(String[] auditData) {
        if (uuidToName == null){
            initialiseUuidToNameMap();
        }

        for (int i = 0; i < auditData.length; i++){
            if (uuidToName.containsKey(auditData[i])){
                auditData[i] = uuidToName.get(auditData[i]);
            }
        }
        return auditData;
    }

    private void initialiseUuidToNameMap() {
        uuidToName = new HashMap<String, String>();

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
}
