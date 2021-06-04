package uk.gov.digital.ho.hocs.audit.export.adapter;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants.*;
import static uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants.DATE_SECTION.*;


@Slf4j
public class DateAdapter implements ExportViewFieldAdapter {

    @Override
    public String getAdapterType() {
        return FIELD_ADAPTER_DATE;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String) {
            String date = (String)input;
            if (date.matches(MALFORMED_DATE_REGEX)) {
                String year = retrieveSection(YEAR, date);
                String month = retrieveSection(MONTH, date);
                String day = retrieveSection(DAY, date);
                if (year != null && month != null && day != null) {
                    return year.substring(year.length() - YEAR_DIGITS) + "-" +
                           month.substring(month.length() - MONTH_DIGITS) + "-" +
                           day.substring(day.length() - DAY_DIGITS);
                }
            }
            return date;
        }
        return null;
    }

    protected String retrieveSection(DATE_SECTION section, String date) {
        switch (section) {
        case DAY:
            return extract(date, EXTRACT_DAY_REGEX);
        case MONTH:
            return extract(date, EXTRACT_MONTH_REGEX);
        case YEAR:
            return extract(date, EXTRACT_YEAR_REGEX);
        }
        return null;
    }

    private String extract(String date, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(date);
        if (matcher.find()) {
            return matcher.group(pattern.equals(EXTRACT_DAY_REGEX) ? 1 : 0);
        }
        return null;
    }
}
