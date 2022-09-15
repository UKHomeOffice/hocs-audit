package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.client.info.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.LogEvent;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.ExportField;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.ExportViewFieldAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.TeamNameAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.TopicNameAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.UnitNameAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.UserEmailAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.UserFirstAndLastNameAdapter;
import uk.gov.digital.ho.hocs.audit.service.domain.adapter.UsernameAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_CUSTOM_CONVERTER_FAILURE;

@Slf4j
@Service
public class CustomExportDataConverter {

    private final InfoClient infoClient;

    private final CaseworkClient caseworkClient;

    private Map<String, ExportViewFieldAdapter> adapters;

    public CustomExportDataConverter(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;

        adapters = new HashMap<>();
    }

    public String[] getHeaders(CustomExportViews.CustomExportView exportView) {
        List<String> headers = new ArrayList<>();

        for (var field : exportView.getFields()) {
            if (!shouldHide(field)) {
                headers.add(field.getName());
            }
        }

        return headers.toArray(String[]::new);
    }

    public Object[] convertData(Object[] input, List<ExportField> fields) {
        Object[] convertedData = null;

        if (input != null) {
            convertedData = convertCustomDataRow(input, fields);
        }

        return convertedData;
    }

    private Object[] convertCustomDataRow(Object[] rawData, List<ExportField> fields) {
        List<String> results = new ArrayList<>();
        int index = 0;
        for (var field : fields) {
            if (!shouldHide(field)) {
                results.add(applyAdapter(rawData[index], field.getAdapter()));
            }
            index++;
        }

        return results.toArray();
    }

    private String applyAdapter(Object data, String adapter) {
        Object result = data;

        if (adapter != null) {
            ExportViewFieldAdapter adapterToUse = adapters.get(adapter);
            if (adapterToUse != null) {
                try {
                    result = adapterToUse.convert(result);
                } catch (Exception e) {
                    log.error("Unable to convert value: {} , reason: {}, event: {}", data, e.getMessage(),
                        value(LogEvent.EVENT, CSV_CUSTOM_CONVERTER_FAILURE));
                }
            } else {
                throw new IllegalArgumentException("Cannot convert data for Adapter Type: " + adapter);
            }
        }

        return result == null ? null : String.valueOf(result);
    }

    private boolean shouldHide(ExportField field) {
        return ExportViewConstants.FIELD_ADAPTER_HIDDEN.equals(field.getAdapter());
    }

    public void initialiseAdapters() {
        Set<UserDto> users = infoClient.getUsers();
        Set<TeamDto> teams = infoClient.getAllTeams();
        Set<UnitDto> units = infoClient.getUnits();
        Set<GetTopicResponse> topics = caseworkClient.getAllCaseTopics();
        List<ExportViewFieldAdapter> adapterList = new ArrayList<>();

        adapterList.add(new UserEmailAdapter(users));
        adapterList.add(new UsernameAdapter(users));
        adapterList.add(new UserFirstAndLastNameAdapter(users));
        adapterList.add(new UnitNameAdapter(teams, units));

        adapterList.add(new TopicNameAdapter(topics));
        adapterList.add(new TeamNameAdapter(teams));

        adapters = adapterList.stream().collect(
            Collectors.toMap(ExportViewFieldAdapter::getAdapterType, adapter -> adapter));
    }

}
