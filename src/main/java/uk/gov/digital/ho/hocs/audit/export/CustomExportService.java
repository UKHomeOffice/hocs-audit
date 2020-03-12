package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.adapter.*;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewFieldAdapterDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewFieldDto;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Service
public class CustomExportService {

    private AuditRepository auditRepository;
    private InfoClient infoClient;

    private Map<String, AbstractExportViewFieldAdapter> adapters;

    public CustomExportService(AuditRepository auditRepository, InfoClient infoClient) {
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.adapters = buildAdapters();
    }

    @Transactional(readOnly = true)
    public void customExport(LocalDate from, LocalDate to, HttpServletResponse response, String code) throws IOException {
        ExportViewDto exportViewDto = infoClient.getExportView(code);
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + exportViewDto.getDisplayName() + ".csv");


        OutputStream buffer = new BufferedOutputStream(response.getOutputStream());
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);

        List<String> headers = buildHeaders(exportViewDto);
        List<Object[]> dataList = auditRepository.getResultsFromView(exportViewDto.getCode());


        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

            dataList.forEach((dataRow) -> {
                try {
                    printer.printRecord(convertCustomData(exportViewDto.getFields(), dataRow));
                    outputWriter.flush();
                } catch (Exception e) {
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
        int index = 0;
        for (ExportViewFieldDto fieldDto : fields) {
            if (!shouldHide(fieldDto)) {
                results.add(applyAdapters(rawData[index], fieldDto.getAdapters()));
            }
            index++;
        }


        return results;
    }

    private String applyAdapters(Object data, List<ExportViewFieldAdapterDto> adaptersDtos) {


        Object result = data;
        for (ExportViewFieldAdapterDto adapterDto : adaptersDtos) {
            AbstractExportViewFieldAdapter adapterToUse = adapters.get(adapterDto.getType());

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

    private Map<String, AbstractExportViewFieldAdapter> buildAdapters() {

        List<AbstractExportViewFieldAdapter> adapterList = new ArrayList<>();

        adapterList.add(new UserEmailAdapter(infoClient));
        adapterList.add(new UsernameAdapter(infoClient));
        adapterList.add(new UserFirstAndLastNameAdapter(infoClient));
        adapterList.add(new TeamNameAdapter(infoClient));
        adapterList.add(new UnitNameAdapter(infoClient));

        return adapterList.stream().collect(Collectors.toMap(ExportViewFieldAdapter::getAdapterType, adapter -> adapter));

    }


}


