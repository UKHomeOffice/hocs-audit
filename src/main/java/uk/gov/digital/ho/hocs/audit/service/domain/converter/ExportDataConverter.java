package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.core.utils.UuidStringChecker;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class ExportDataConverter {

    private final CaseworkClient caseworkClient;
    private final Map<String, String> uuidToName;
    private final Map<String, String> entityListItemToName;
    private final boolean convert;

    public ExportDataConverter(Map<String, String> uuidToName,
                               Map<String, String> entityListItemToName, CaseworkClient caseworkClient) {
        this(true, uuidToName, entityListItemToName, caseworkClient);
    }

    public ExportDataConverter() {
        this(false, null, null, null);
    }

    private ExportDataConverter(boolean convert, Map<String, String> uuidToName,
                                Map<String, String> entityListItemToName, CaseworkClient caseworkClient) {
        this.convert = convert;
        this.uuidToName = uuidToName;
        this.entityListItemToName = entityListItemToName;
        this.caseworkClient = caseworkClient;
    }

    public String convertValue(String value) {
        if (!convert && value != null) {
            return value;
        }

        if (UuidStringChecker.isUUID(value)) {
            return uuidToName.getOrDefault(value, value);
        }

        return entityListItemToName.getOrDefault(value, value).replace(",", "");
    }

    public String convertCaseUuid(UUID value) {
        if (!convert) {
            return value.toString();
        }

        GetCaseReferenceResponse caseReferenceResponse = caseworkClient.getCaseReference(value.toString());

        String referenceNotFound = "REFERENCE NOT FOUND";
        if (StringUtils.hasText(caseReferenceResponse.getReference()) &&
                // if the reference is not found, the uuid does not refer to a case, and can pass through
                !caseReferenceResponse.getReference().equals(referenceNotFound)) {
            return caseReferenceResponse.getReference();
        }

        return value.toString();
    }
}
