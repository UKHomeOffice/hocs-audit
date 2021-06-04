package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants.*;
import static uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants.DATE_SECTION.*;

@RunWith (SpringRunner.class)
public class DateAdapterTest {

    String TEXT_DATA = "ABC-12-EE";
    String SIMILAR_DATE = "2021.02.11";
    String VALID_DATE = "2021-02-28";
    String MALFORMED_DATE_Y = "02021-01-22";
    String MALFORMED_DATE_M = "2022-002-23";
    String MALFORMED_DATE_D = "2023-03-024";
    String MALFORMED_DATE_YMD = "02024-004-025";

    @Test
    public void shouldFindMalformedDatesButNotNonDates() {
        assertThat(TEXT_DATA.matches(MALFORMED_DATE_REGEX)).isFalse();
        assertThat(SIMILAR_DATE.matches(MALFORMED_DATE_REGEX)).isFalse();
        assertThat(VALID_DATE.matches(MALFORMED_DATE_REGEX)).isTrue();
        
        assertThat(MALFORMED_DATE_D.matches(MALFORMED_DATE_REGEX)).isTrue();
        assertThat(MALFORMED_DATE_M.matches(MALFORMED_DATE_REGEX)).isTrue();
        assertThat(MALFORMED_DATE_Y.matches(MALFORMED_DATE_REGEX)).isTrue();
        assertThat(MALFORMED_DATE_YMD.matches(MALFORMED_DATE_REGEX)).isTrue();
    }

    @Test
    public void shouldExtractPartsOfDate() {
        DateAdapter dateAdapter = new DateAdapter();
        assertThat(dateAdapter.retrieveSection(YEAR, MALFORMED_DATE_Y)).isEqualTo("02021");
        assertThat(dateAdapter.retrieveSection(MONTH, MALFORMED_DATE_M)).isEqualTo("002");
        assertThat(dateAdapter.retrieveSection(DAY, MALFORMED_DATE_D)).isEqualTo("024");

        assertThat(dateAdapter.retrieveSection(DAY, MALFORMED_DATE_Y)).isEqualTo("22");
        assertThat(dateAdapter.retrieveSection(YEAR, MALFORMED_DATE_M)).isEqualTo("2022");
        assertThat(dateAdapter.retrieveSection(MONTH, MALFORMED_DATE_D)).isEqualTo("03");
    }

    @Test
    public void shouldConvertDates() {
        DateAdapter dateAdapter = new DateAdapter();
        assertThat(dateAdapter.convert(TEXT_DATA)).isEqualTo(TEXT_DATA);
        assertThat(dateAdapter.convert(SIMILAR_DATE)).isEqualTo(SIMILAR_DATE);
        assertThat(dateAdapter.convert(VALID_DATE)).isEqualTo("2021-02-28");
        assertThat(dateAdapter.convert(MALFORMED_DATE_Y)).isEqualTo("2021-01-22");
        assertThat(dateAdapter.convert(MALFORMED_DATE_M)).isEqualTo("2022-02-23");
        assertThat(dateAdapter.convert(MALFORMED_DATE_D)).isEqualTo("2023-03-24");
        assertThat(dateAdapter.convert(MALFORMED_DATE_YMD)).isEqualTo("2024-04-25");
    }

}
