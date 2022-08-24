package uk.gov.digital.ho.hocs.audit.repository.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CaseDataFields {

    @JsonValue
    private final Map<String, LinkedHashSet<String>> caseTypeExportFields;

    @JsonCreator
    public CaseDataFields(Map<String, LinkedHashSet<String>> caseTypeExportFields) {
        this.caseTypeExportFields = caseTypeExportFields;
    }

    public Set<String> getCaseDataFieldsForCaseType(String caseType) {
        return caseTypeExportFields.get(caseType);
    }

}
