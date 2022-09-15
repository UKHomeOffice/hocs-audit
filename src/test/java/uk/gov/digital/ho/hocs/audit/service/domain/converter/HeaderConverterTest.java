package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HeaderConverterTest {

    @Autowired
    private HeaderConverter converter;

    @Test
    public void convertDataHandlesEmptyArray() {
        String[] substitutedHeaders = converter.substitute(new String[0]);

        Assertions.assertNotNull(substitutedHeaders);
        Assertions.assertEquals(0, substitutedHeaders.length);
    }

    @Test
    public void convertDataHandlesNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.substitute(null));
    }

    @Test
    public void substituteExistingHeadersWithOrder() {
        String[] headers = new String[] { "title1", "title2", "title3", "title4" };
        String[] expectedHeaders = new String[] { "New Title 1", "New Title 2", "New Title 3", "New Title 4" };
        String[] substitutedHeaders = converter.substitute(headers);

        Assertions.assertArrayEquals(substitutedHeaders, expectedHeaders);
    }

    @Test
    public void substituteNonExistingHeadersWithOrder() {
        String[] headers = new String[] { "NonTitle1", "NonTitle2", "NonTitle3", "NonTitle4" };
        String[] expectedHeaders = new String[] { "NonTitle1", "NonTitle2", "NonTitle3", "NonTitle4" };
        String[] substitutedHeaders = converter.substitute(headers);

        Assertions.assertArrayEquals(substitutedHeaders, expectedHeaders);
    }

    @Test
    public void substituteExistingAndNonExistingMixedHeadersWithOrder() {
        String[] headers = new String[] { "NonTitle1", "title1", "title2", "NonTitle2", "NonTitle3", "title3",
            "NonTitle4", "title4" };
        String[] expectedHeaders = new String[] { "NonTitle1", "New Title 1", "New Title 2", "NonTitle2", "NonTitle3",
            "New Title 3", "NonTitle4", "New Title 4" };
        String[] substitutedHeaders = converter.substitute(headers);

        Assertions.assertArrayEquals(substitutedHeaders, expectedHeaders);
    }

}
