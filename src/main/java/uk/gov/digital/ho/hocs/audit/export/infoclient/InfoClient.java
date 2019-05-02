package uk.gov.digital.ho.hocs.audit.export.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.GetCaseTypesResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;

import java.util.LinkedHashSet;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Component
public class InfoClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public InfoClient(RestHelper restHelper,
                      @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    public Set<CaseTypeDto> getCaseTypes() {
        GetCaseTypesResponse response = restHelper.get(serviceBaseURL, "/caseType", GetCaseTypesResponse.class);

        log.info("Got {} case types", response.caseTypes.size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return response.caseTypes;
    }

    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

   public LinkedHashSet<String> getCaseExportFields(String caseType) {
       LinkedHashSet<String> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/reporting", caseType), new ParameterizedTypeReference<LinkedHashSet<String>>() {});
        log.info("Got {} case reporting fields for CaseType {}", response.size(), caseType, value(EVENT, INFO_CLIENT_GET_EXPORT_FIELDS_SUCCESS));
        return response;
    }




}