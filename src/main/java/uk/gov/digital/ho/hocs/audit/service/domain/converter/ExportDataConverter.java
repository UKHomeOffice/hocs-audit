package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.core.utils.UuidStringChecker;

import java.util.Map;
import java.util.Set;

@Slf4j
public class ExportDataConverter {

    private static final Set<String> CASE_TYPE_SHORT_CODES = Set.of("b5", "b6");

    private final CaseworkClient caseworkClient;
    private final Map<String, String> uuidToName;
    private final Map<String, String> entityListItemToName;

    private final String REFERENCE_NOT_FOUND = "REFERENCE NOT FOUND";

    ExportDataConverter
            (Map<String, String> uuidToName, Map<String, String> entityListItemToName, CaseworkClient caseworkClient) {
        this.caseworkClient = caseworkClient;
        this.uuidToName = uuidToName;
        this.entityListItemToName = entityListItemToName;
    }

    public String[] convertData(String[] auditData, String caseShortCode) {
        for (int i = 0; i < auditData.length; i++){
            String fieldData = auditData[i];
            if (UuidStringChecker.isUUID(fieldData)) {
                if (uuidToName.containsKey(fieldData)) {
                    auditData[i] = uuidToName.get(fieldData);
                } else {
                    GetCaseReferenceResponse caseReferenceResponse = caseworkClient.getCaseReference(fieldData);

                    if (StringUtils.hasText(caseReferenceResponse.getReference()) &&
                            // if the reference is not found, the uuid does not refer to a case, and can pass through
                            !caseReferenceResponse.getReference().equals(REFERENCE_NOT_FOUND)) {
                        uuidToName.put(fieldData, caseReferenceResponse.getReference());
                        auditData[i] = caseReferenceResponse.getReference();
                    }
                }
            } else {
                if (CASE_TYPE_SHORT_CODES.contains(caseShortCode)) {
                    if (entityListItemToName.containsKey(fieldData)) {
                        String displayValue = entityListItemToName.get(fieldData);
                        String sanitizedDisplayValue = sanitiseForCsv(displayValue);
                        auditData[i] = sanitizedDisplayValue;
                    }
                }
            }
        }
        return auditData;
    }

    private String sanitiseForCsv(String value) {
        return value.replace(",", "");
    }
}
