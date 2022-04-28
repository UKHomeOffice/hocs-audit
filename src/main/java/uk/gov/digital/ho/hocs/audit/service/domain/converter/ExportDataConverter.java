package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import uk.gov.digital.ho.hocs.audit.core.utils.UuidStringChecker;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.CaseReference;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExportDataConverter {

    private final Map<String, String> uuidToName;
    private final Map<String, String> caseUUIDToCaseRef;
    private final Map<String, String> entityListItemToName;
    private final boolean convert;

    public ExportDataConverter(Map<String, String> uuidToName,
                               Map<String, String> entityListItemToName,
                               String caseType,
                               AuditRepository auditRepository) {
        this(true, uuidToName, entityListItemToName, caseType, auditRepository);
    }

    public ExportDataConverter() {
        this(false, null, null, null, null);
    }

    private ExportDataConverter(boolean convert, Map<String, String> uuidToName,
                                Map<String, String> entityListItemToName,
                                String caseType,
                                AuditRepository auditRepository) {
        this.convert = convert;
        this.uuidToName = uuidToName;
        this.entityListItemToName = entityListItemToName;

        if (convert) {
            this.caseUUIDToCaseRef = auditRepository.getCaseReferencesForType(caseType).collect(Collectors.toMap(CaseReference::getCaseUUID, CaseReference::getCaseReference));
        } else {
            this.caseUUIDToCaseRef = null;
        }
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
        var uuidString = value.toString();

        if (!convert) {
            return uuidString;
        }

        return caseUUIDToCaseRef.getOrDefault(uuidString, uuidString);
    }
}
