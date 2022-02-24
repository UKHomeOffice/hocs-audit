package uk.gov.digital.ho.hocs.audit.export.parsers.interests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClientSupplier;
import uk.gov.digital.ho.hocs.audit.export.parsers.AbstractDataParser;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractInterestDataParser extends AbstractDataParser {

    protected AbstractInterestDataParser(InfoClientSupplier infoClientSupplier, CaseworkClient caseworkClient,
                                         ObjectMapper objectMapper, ZonedDateTimeConverter zonedDateTimeConverter,
                                         boolean convert) {
        super(infoClientSupplier, caseworkClient, objectMapper, zonedDateTimeConverter, convert);
    }

    @Override
    protected Stream<Supplier<Map<String, String>>> getUuidSuppliers() {
        return Stream.of(
                infoClientSupplier.getUsers()
        );
    }

    @Override
    public String[] parsePayload(AuditData audit) throws JsonProcessingException {
        AuditPayload.Interest interestData = objectMapper.readValue(audit.getAuditPayload(), AuditPayload.Interest.class);

        return new String[] {
                zonedDateTimeConverter.convert(audit.getAuditTimestamp()),
                audit.getType(),
                convertValue(audit.getUserID()),
                convertCaseUuid(Objects.toString(audit.getCaseUUID(), "")),
                convertValue(Objects.toString(interestData.getPartyType(), "")),
                Objects.toString(interestData.getInterestDetails(), "")
        };
    }
}
