package uk.gov.digital.ho.hocs.audit.repository.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Set;

public class CaseDataFields {

    @JsonValue
    private final Map<String, Set<String>> caseTypeExportFields;

    @JsonCreator
    public CaseDataFields(Map<String, Set<String>> caseTypeExportFields) {
        this.caseTypeExportFields = caseTypeExportFields;
    }

    public Set<String> getCaseDataFieldsForCaseType(String caseType) {
        return caseTypeExportFields.get(caseType);
    }

}
