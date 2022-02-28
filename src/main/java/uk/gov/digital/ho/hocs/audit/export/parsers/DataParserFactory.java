package uk.gov.digital.ho.hocs.audit.export.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClientSupplier;
import uk.gov.digital.ho.hocs.audit.export.parsers.interests.BfInterestDataParser;
import uk.gov.digital.ho.hocs.audit.export.parsers.interests.FoiInterestDataParser;

@Service
public class DataParserFactory {

    private final InfoClientSupplier infoClientSupplier;
    private final CaseworkClient caseworkClient;
    private final ObjectMapper objectMapper;

    public DataParserFactory(InfoClientSupplier infoClientSupplier,
                             CaseworkClient caseworkClient,
                             ObjectMapper objectMapper) {
        this.infoClientSupplier = infoClientSupplier;
        this.caseworkClient = caseworkClient;
        this.objectMapper = objectMapper;
    }

    public AbstractDataParser getInterestInstance(String caseType, boolean convert, ZonedDateTimeConverter zonedDateTimeConverter)
        throws IllegalArgumentException
    {
        switch (caseType) {
            case "FOI": {
                return new FoiInterestDataParser(infoClientSupplier, caseworkClient, objectMapper,
                        zonedDateTimeConverter, convert);
            }
            case "BF": {
                return new BfInterestDataParser(infoClientSupplier, caseworkClient, objectMapper,
                        zonedDateTimeConverter, convert);
            }
            default:
                throw new IllegalArgumentException(String.format("CaseType not supported: %s", caseType));
        }
    }
    
}
