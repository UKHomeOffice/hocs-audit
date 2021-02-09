package uk.gov.digital.ho.hocs.audit.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter that for the user to pass in a local date time and to return the offset
 * against a specified time zone.
 */
@Slf4j
public class ZonedDateTimeConverter {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private ZoneId specifiedTimeZoneId = ZoneId.of("Europe/London");

    /**
     * Constructor that allows for a user specified date format.
     * @param outputtedDateFormat the date format to output against.
     * @param timeZoneId the timezone to set against.
     */
    public ZonedDateTimeConverter(final String outputtedDateFormat, final String timeZoneId) {
        if (StringUtils.hasText(outputtedDateFormat)) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(outputtedDateFormat);
        }

        if (StringUtils.hasText(timeZoneId)) {
            this.specifiedTimeZoneId = ZoneId.of(timeZoneId);
        }
    }

    /**
     * A helper to return the timestamp as an offset against the specified time zone.
     * @param localDateTime the time you want to show the offset of.
     * @return the offsetted date time string
     */
    public String convert(final LocalDateTime localDateTime) {
        log.info("Timestamp before convert: {}", localDateTime);
        final ZonedDateTime zonedDateTime =
                localDateTime
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(specifiedTimeZoneId);
        log.info("Timestamp after time zone: {}", zonedDateTime);
        log.info("Timestamp after toLocalDateTime(): {}", zonedDateTime.toLocalDateTime());
        log.info("Timestamp after formatting: {}", zonedDateTime.toLocalDateTime().format(dateTimeFormatter));
        return zonedDateTime.toLocalDateTime().format(dateTimeFormatter);
    }

}
