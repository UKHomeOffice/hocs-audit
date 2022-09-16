package uk.gov.digital.ho.hocs.audit.core.utils;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter that for the user to pass in a local date time and to return the offset
 * against a specified time zone.
 */
public class ZonedDateTimeConverter {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    private static final String DEFAULT_ZONE_ID = "Europe/London";

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    private ZoneId specifiedTimeZoneId = ZoneId.of(DEFAULT_ZONE_ID);

    /*
     * Default constructor that initialises the formatter and zone id to the default.
     */
    public ZonedDateTimeConverter() {
        this("", "");
    }

    /**
     * Constructor that allows for a user specified date format.
     *
     * @param outputtedDateFormat the date format to output against.
     * @param timeZoneId          the timezone to set against.
     */
    public ZonedDateTimeConverter(String outputtedDateFormat, String timeZoneId) {
        if (StringUtils.hasText(outputtedDateFormat)) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(outputtedDateFormat);
        }

        if (StringUtils.hasText(timeZoneId)) {
            this.specifiedTimeZoneId = ZoneId.of(timeZoneId);
        }
    }

    /**
     * A helper to return the timestamp as an offset against the specified time zone.
     *
     * @param localDateTime the time you want to show the offset of.
     *
     * @return the offsetted date time string
     */
    public String convert(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(
            specifiedTimeZoneId);

        return zonedDateTime.toLocalDateTime().format(dateTimeFormatter);
    }

}
