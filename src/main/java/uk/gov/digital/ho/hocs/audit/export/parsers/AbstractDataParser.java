package uk.gov.digital.ho.hocs.audit.export.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClientSupplier;
import uk.gov.digital.ho.hocs.audit.utils.UuidStringChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractDataParser {

    protected final ZonedDateTimeConverter zonedDateTimeConverter;
    protected final ObjectMapper objectMapper;
    protected final InfoClientSupplier infoClientSupplier;
    protected final boolean convert;
    private final CaseworkClient caseworkClient;

    protected final Map<String, String> uuidToName;
    protected final Map<String, String> entityListItemToName;

    protected AbstractDataParser(InfoClientSupplier infoClientSupplier,
                                 CaseworkClient caseworkClient,
                                 ObjectMapper objectMapper,
                                 ZonedDateTimeConverter zonedDateTimeConverter,
                                 boolean convert) {
        this.infoClientSupplier = infoClientSupplier;
        this.convert = convert;
        this.zonedDateTimeConverter = zonedDateTimeConverter;
        this.objectMapper = objectMapper;
        this.caseworkClient = caseworkClient;

        uuidToName = new HashMap<>();
        entityListItemToName = new HashMap<>();

        if (convert) {
            initialiseConversionValues(getUuidSuppliers(), getEntityLists());
        }
    }

    private void initialiseConversionValues(Stream<Supplier<Map<String, String>>> uuidSuppliers,
                                            List<String> entityLists) {
        uuidToName.putAll(
                uuidSuppliers
                        .map(CompletableFuture::supplyAsync)
                        .map(CompletableFuture::join)
                        .collect(HashMap::new, Map::putAll, Map::putAll));

        for (String listName: entityLists) {
            entityListItemToName.putAll(infoClientSupplier.getEntityList(listName).get());
        }
    }


    protected String convertValue(String value) {
        if (!convert && value != null) {
            return value;
        }

        if (UuidStringChecker.isUUID(value))  {
            return uuidToName.getOrDefault(value, value);
        }

        return entityListItemToName.getOrDefault(value, value);
    }

    protected String convertCaseUuid(String value) {
        if (!convert || !UuidStringChecker.isUUID(value)) {
            return value;
        }

        GetCaseReferenceResponse caseReferenceResponse = caseworkClient.getCaseReference(value);

        String referenceNotFound = "REFERENCE NOT FOUND";
        if (StringUtils.hasText(caseReferenceResponse.getReference()) &&
                // if the reference is not found, the uuid does not refer to a case, and can pass through
                !caseReferenceResponse.getReference().equals(referenceNotFound)) {
            return caseReferenceResponse.getReference();
        }

        return value;
    }

    public abstract String[] parsePayload(AuditData audit) throws JsonProcessingException;
    protected abstract List<String> getEntityLists();
    protected abstract Stream<Supplier<Map<String, String>>> getUuidSuppliers();

}
