package uk.gov.digital.ho.hocs.audit.export.parsers.interests;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClientSupplier;

import java.util.List;

public class BfInterestDataParser extends AbstractInterestDataParser {

    public BfInterestDataParser(InfoClientSupplier infoClientSupplier, CaseworkClient caseworkClient,
                                ObjectMapper objectMapper, ZonedDateTimeConverter zonedDateTimeConverter,
                                boolean convert) {
        super(infoClientSupplier, caseworkClient, objectMapper, zonedDateTimeConverter, convert);
    }

    @Override
    public List<String> getEntityLists() {
        return List.of("BF_INTERESTED_PARTIES");
    }

}
