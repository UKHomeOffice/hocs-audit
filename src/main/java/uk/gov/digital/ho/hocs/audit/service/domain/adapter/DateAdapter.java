package uk.gov.digital.ho.hocs.audit.service.domain.adapter;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.digital.ho.hocs.audit.client.info.ExportViewConstants.FIELD_ADAPTER_DATE;
import static uk.gov.digital.ho.hocs.audit.client.info.ExportViewConstants.GROUPED_DATE_REGEX;

@Slf4j
public class DateAdapter implements ExportViewFieldAdapter {

    private static final Pattern DATE_PATTERN = Pattern.compile(GROUPED_DATE_REGEX);

    @Override
    public String getAdapterType() {
        return FIELD_ADAPTER_DATE;
    }

    /**
     * The aim of this function is to identify dates that are malformed by having a zero in front of the
     * year, month or day (0yyyy-0mm-0dd). The REGEX used will identify any combination of the zero leading digits.
     * Once identified, this function converts the date to a format without the leading zero (yyyy-mm-dd).
     *
     * @param input The ExportViewFieldAdapter interface supports any kind of object but the calling code only ever passes a string.
     *
     * @return A string value of the converted (corrected) date or the original value if it is not a date.
     */
    @Override
    public String convert(Object input) {
        if (input instanceof String) {
            String date = (String) input;
            Matcher matcher = DATE_PATTERN.matcher(date);
            if (matcher.find()) {
                String year = matcher.group(1);
                String month = matcher.group(2);
                String day = matcher.group(3);
                return year + "-" + month + "-" + day;
            }
            return date;
        }
        return input != null ? input.toString() : null;
    }

}
