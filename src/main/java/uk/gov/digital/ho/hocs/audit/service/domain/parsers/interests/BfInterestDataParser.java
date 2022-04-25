package uk.gov.digital.ho.hocs.audit.service.domain.parsers.interests;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClientSupplier;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;

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
