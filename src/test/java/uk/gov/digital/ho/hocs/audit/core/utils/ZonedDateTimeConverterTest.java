package uk.gov.digital.ho.hocs.audit.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

public class ZonedDateTimeConverterTest {

    @Test
    public void shouldShowSpecifiedFormatWhenTimestampFormatValid() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter("yyyy-MM-dd", null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12", convertedDateTime);
    }

    @Test
    public void shouldShowDefaultFormatWhenTimestampFormatNull() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12T00:00:00.000000", convertedDateTime);
    }

    @Test
    public void shouldShowDefaultFormatWhenTimestampFormatEmpty() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter("", null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12T00:00:00.000000", convertedDateTime);
    }

    @Test
    public void shouldThrowExceptionIfTimestampFormatInvalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ZonedDateTimeConverter("INVALID", null);
        });
    }

    @Test
    public void shouldShowDefaultFormatWhenTimestampFormatValid() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, "Europe/Rome");

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12T01:00:00.000000", convertedDateTime);
    }

    @Test
    public void shouldShowSystemDefaultWhenZoneIdNull() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12T00:00:00.000000", convertedDateTime);
    }

    @Test
    public void shouldShowSystemDefaultWhenZoneIdEmpty() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, "");

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        Assertions.assertNotNull(convertedDateTime);
        Assertions.assertEquals("2020-12-12T00:00:00.000000", convertedDateTime);
    }

    @Test
    public void shouldThrowExceptionIfZoneIdInvalid() {
        Assertions.assertThrows(ZoneRulesException.class, () ->
                new ZonedDateTimeConverter(null, "INVALID"));
    }

}
