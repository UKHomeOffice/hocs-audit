package uk.gov.digital.ho.hocs.audit.service.domain.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.digital.ho.hocs.audit.client.info.ExportViewConstants.GROUPED_DATE_REGEX;

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
        assertFalse(TEXT_DATA.matches(GROUPED_DATE_REGEX));
        assertFalse(SIMILAR_DATE.matches(GROUPED_DATE_REGEX));
        assertTrue(VALID_DATE.matches(GROUPED_DATE_REGEX));

        assertTrue(MALFORMED_DATE_D.matches(GROUPED_DATE_REGEX));
        assertTrue(MALFORMED_DATE_M.matches(GROUPED_DATE_REGEX));
        assertTrue(MALFORMED_DATE_Y.matches(GROUPED_DATE_REGEX));
        assertTrue(MALFORMED_DATE_YMD.matches(GROUPED_DATE_REGEX));
    }

    @Test
    public void shouldConvertDates() {
        DateAdapter dateAdapter = new DateAdapter();

        assertEquals(TEXT_DATA, dateAdapter.convert(TEXT_DATA));
        assertEquals(SIMILAR_DATE, dateAdapter.convert(SIMILAR_DATE));
        assertEquals("2021-02-28", dateAdapter.convert(VALID_DATE));
        assertEquals("2021-01-22", dateAdapter.convert(MALFORMED_DATE_Y));
        assertEquals("2022-02-23", dateAdapter.convert(MALFORMED_DATE_M));
        assertEquals("2023-03-24", dateAdapter.convert(MALFORMED_DATE_D));
        assertEquals("2024-04-25", dateAdapter.convert(MALFORMED_DATE_YMD));
    }

    @Test
    public void shouldDoAllCombinations() {
        DateAdapter dateAdapter = new DateAdapter();
        String yearString, monthString, dayString, zeroYear, zeroMonth, zeroDay;
        for (int year = 1900; year <= 2050; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= 31; day++) {
                    yearString = Integer.toString(year);
                    monthString = month < 10 ? "0" + month : Integer.toString(month);
                    dayString = day < 10 ? "0" + day : Integer.toString(day);
                    zeroYear = "0" + yearString;
                    zeroMonth = "0" + monthString;
                    zeroDay = "0" + dayString;
                    assertEquals(yearString + "-" + monthString + "-" + dayString,
                        dateAdapter.convert(zeroYear + "-" + zeroMonth + "-" + zeroDay));
                    assertEquals(yearString + "-" + monthString + "-" + dayString,
                        dateAdapter.convert(yearString + "-" + zeroMonth + "-" + zeroDay));
                    assertEquals(yearString + "-" + monthString + "-" + dayString,
                        dateAdapter.convert(yearString + "-" + monthString + "-" + zeroDay));
                    assertEquals(yearString + "-" + monthString + "-" + dayString,
                        dateAdapter.convert(yearString + "-" + monthString + "-" + dayString));
                }
            }
        }
    }

}
