package uk.gov.digital.ho.hocs.audit.export.converter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;

import java.util.Map;
import java.util.Set;

@Slf4j
public class ExportDataConverter {

    private static final String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private static final Set<String> MPAM_SHORT_CODES = Set.of("b5", "b6");

    private final CaseworkClient caseworkClient;
    private final Map<String, String> uuidToName;
    private final Map<String, String> mpamCodeToName;

    private final String REFERENCE_NOT_FOUND = "REFERENCE NOT FOUND";

    ExportDataConverter
            (Map<String, String> uuidToName, Map<String, String> mpamCodeToName, CaseworkClient caseworkClient) {
        this.caseworkClient = caseworkClient;
        this.uuidToName = uuidToName;
        this.mpamCodeToName = mpamCodeToName;
    }

    public String[] convertData(String[] auditData, String caseShortCode) {
        for (int i = 0; i < auditData.length; i++){
            String fieldData = auditData[i];
            if (isUUID(fieldData)) {
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
                if (MPAM_SHORT_CODES.contains(caseShortCode)) {
                    if (mpamCodeToName.containsKey(fieldData)) {
                        String displayValue = mpamCodeToName.get(fieldData);
                        String sanitizedDisplayValue = sanitiseForCsv(displayValue);
                        auditData[i] = sanitizedDisplayValue;
                    }
                }
            }
        }
        return auditData;
    }

    boolean isUUID(String uuid) {
        if (StringUtils.hasText(uuid)) {
            return uuid.matches(UUID_REGEX);
        }
        return false;
    }

    private String sanitiseForCsv(String value) {
        return value.replace(",", "");
    }
}
