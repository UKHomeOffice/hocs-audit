package uk.gov.digital.ho.hocs.audit.service.domain.parsers.interests;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClientSupplier;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;

import java.util.List;

public class FoiInterestDataParser extends AbstractInterestDataParser {

    public FoiInterestDataParser(InfoClientSupplier infoClientSupplier, CaseworkClient caseworkClient,
                                 ObjectMapper objectMapper, ZonedDateTimeConverter zonedDateTimeConverter,
                                 boolean convert) {
        super(infoClientSupplier, caseworkClient, objectMapper, zonedDateTimeConverter, convert);
    }

    @Override
    protected List<String> getEntityLists() {
        return List.of("FOI_INTERESTED_PARTIES");
    }

}
