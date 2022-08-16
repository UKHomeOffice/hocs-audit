package uk.gov.digital.ho.hocs.audit.repository.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews;

@Service
public class CustomExportViewsReader extends JsonConfigFileReader {

    private final CustomExportViews customExportViews;

    public CustomExportViewsReader(ObjectMapper objectMapper) {
        super(objectMapper);

        customExportViews = readValueFromFile(new TypeReference<>() {});
    }

    @Override
    String getFileName() {
        return "custom-export-views";
    }

    public CustomExportViews.CustomExportView getByViewName(String caseType) {
        return customExportViews.getCustomViewByName(caseType);
    }

}
