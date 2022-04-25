package uk.gov.digital.ho.hocs.audit.service.domain.parsers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.parsers.interests.BfInterestDataParser;
import uk.gov.digital.ho.hocs.audit.service.domain.parsers.interests.FoiInterestDataParser;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataParserFactoryTest {

    @Autowired
    private DataParserFactory dataParserFactory;

    @Test
    public void shouldReturnFoiInterestParserWithCaseType() {
        var result = dataParserFactory.getInterestInstance("FOI", false,
                new ZonedDateTimeConverter(null, null));
        Assert.assertEquals(result.getClass(), FoiInterestDataParser.class);
    }

    @Test
    public void shouldReturnBfInterestParserWithCaseType() {
        var result = dataParserFactory.getInterestInstance("BF", false,
                new ZonedDateTimeConverter(null, null));
        Assert.assertEquals(result.getClass(), BfInterestDataParser.class);
    }

    @Test
    public void shouldThrowExceptionIfCaseTypeNotInInterestAllowed() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> dataParserFactory.getInterestInstance("TEST", true,
                        new ZonedDateTimeConverter(null, null)));
    }

}
