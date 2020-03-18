package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.export.adapter.*;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.CSV_CUSTOM_CONVERTER_FAILURE;

@Slf4j
@Service
public class CustomExportDataConverter {

    private InfoClient infoClient;
    private CaseworkClient caseworkClient;
    private Map<String, ExportViewFieldAdapter> adapters;

    public CustomExportDataConverter(InfoClient infoClient, CaseworkClient caseworkClient) {
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;
    }

    public List<String> getHeaders(ExportViewDto exportViewDto) {
        List<String> headers = new ArrayList<>();

        for (ExportViewFieldDto viewFieldDto : exportViewDto.getFields()) {
            if (!shouldHide(viewFieldDto)) {
                headers.add(viewFieldDto.getDisplayName());
            }
        }

        return headers;
    }

    public List<Object[]> convertData(List<Object[]> input, List<ExportViewFieldDto> fields) {

        if (!CollectionUtils.isEmpty(input)) {
            initialiseAdapters();
            List<Object[]> convertedData = new ArrayList<>();

            for (Object[] row : input) {
                convertedData.add(convertCustomDataRow(row, fields));
            }

            return convertedData;

        }

        return new ArrayList<>();

    }

    private Object[] convertCustomDataRow(Object[] rawData, List<ExportViewFieldDto> fields) {

        List<String> results = new ArrayList<>();
        int index = 0;
        for (ExportViewFieldDto fieldDto : fields) {
            if (!shouldHide(fieldDto)) {
                results.add(applyAdapters(rawData[index], fieldDto.getAdapters()));
            }
            index++;
        }


        return results.toArray();
    }

    private String applyAdapters(Object data, List<ExportViewFieldAdapterDto> adaptersDtos) {


        Object result = data;
        for (ExportViewFieldAdapterDto adapterDto : adaptersDtos) {
            ExportViewFieldAdapter adapterToUse = adapters.get(adapterDto.getType());

            if (adapterToUse != null) {
                try {
                    result = adapterToUse.convert(result);
                } catch (Exception e) {
                    log.error("Unable to convert value: {} , reason: {}, event: {}", data, e.getMessage(), value(LogEvent.EVENT, CSV_CUSTOM_CONVERTER_FAILURE));
                }
            } else {
                throw new IllegalArgumentException("Cannot convert data for Adapter Type: " + adapterDto.getType());
            }
        }

        return result == null ? null : String.valueOf(result);

    }

    private boolean shouldHide(ExportViewFieldDto viewFieldDto) {

        for (ExportViewFieldAdapterDto adapterDto : viewFieldDto.getAdapters()) {
            if (ExportViewConstants.FIELD_ADAPTER_HIDDEN.equals(adapterDto.getType())) {
                return true;
            }
        }

        return false;
    }

    private void initialiseAdapters() {
        Set<UserDto> users = infoClient.getUsers();
        Set<TeamDto> teams = infoClient.getTeams();
        Set<UnitDto> units = infoClient.getUnits();
        Set<GetTopicResponse> topics = caseworkClient.getAllCaseTopics();
        List<ExportViewFieldAdapter> adapterList = new ArrayList<>();

        adapterList.add(new UserEmailAdapter(users));
        adapterList.add(new UsernameAdapter(users));
        adapterList.add(new UserFirstAndLastNameAdapter(users));
        adapterList.add(new UnitNameAdapter(teams, units));


        adapterList.add(new TopicNameAdapter(topics));
        adapterList.add(new TeamNameAdapter(teams));

        adapters = adapterList.stream().collect(Collectors.toMap(ExportViewFieldAdapter::getAdapterType, adapter -> adapter));

    }
}
