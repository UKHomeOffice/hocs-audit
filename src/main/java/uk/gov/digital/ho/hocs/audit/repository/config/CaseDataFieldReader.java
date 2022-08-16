package uk.gov.digital.ho.hocs.audit.repository.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CaseDataFields;

import java.util.Set;

@Service
public class CaseDataFieldReader extends JsonConfigFileReader {

    private final CaseDataFields caseDataFields;

    public CaseDataFieldReader(ObjectMapper objectMapper) {
        super(objectMapper);

        caseDataFields = readValueFromFile(new TypeReference<>() {});
    }

    @Override
    String getFileName() {
        return "case-data-fields";
    }

    public Set<String> getByCaseType(String caseType) {
        var fields = caseDataFields.getCaseDataFieldsForCaseType(caseType);
        return fields == null ? Set.of() : fields;
    }

}
