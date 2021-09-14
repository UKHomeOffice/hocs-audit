package uk.gov.digital.ho.hocs.audit.export.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportDataConverterFactory {

    private static final String[] MPAM_CODE_MAPPING_LISTS = { "MPAM_ENQUIRY_SUBJECTS", "MPAM_ENQUIRY_REASONS_ALL", "MPAM_BUS_UNITS_ALL" };

    private final InfoClient infoClient;
    private final CaseworkClient caseworkClient;

    public ExportDataConverterFactory(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;
    }

    public ExportDataConverter getInstance() {
        Map<String, String> uuidToName = new HashMap<>();
        Map<String, String> mpamCodeToName = new HashMap<>();

        uuidToName.putAll(infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));
        uuidToName.putAll(infoClient.getAllTeams().stream()
                .collect(Collectors.toMap(team -> team.getUuid().toString(), TeamDto::getDisplayName)));
        uuidToName.putAll(infoClient.getUnits().stream()
                .collect(Collectors.toMap(UnitDto::getUuid, UnitDto::getDisplayName)));
        caseworkClient.getAllCaseTopics()
                .forEach(
                        topic -> uuidToName.putIfAbsent(topic.getTopicUUID().toString(), topic.getTopicText())
                );
        uuidToName.putAll(caseworkClient.getAllActiveCorrespondents().stream()
                .collect(Collectors.toMap(corr -> corr.getUuid().toString(), GetCorrespondentOutlineResponse::getFullname)));

        for (String listName : MPAM_CODE_MAPPING_LISTS) {
            Set<EntityDto> entities = infoClient.getEntitiesForList(listName);
            entities.forEach(e -> mpamCodeToName.put(e.getSimpleName(), e.getData().getTitle()));
        }

        return new ExportDataConverter(uuidToName, mpamCodeToName, caseworkClient);
    }
}
