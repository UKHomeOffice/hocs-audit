package uk.gov.digital.ho.hocs.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ZonedDateTimeConverterTest {

    @Test
    public void shouldShowSpecifiedFormatWhenTimestampFormatValid() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter("yyyy-MM-dd", null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12");
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

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12 00:00:00");
    }

    @Test
    public void shouldShowDefaultFormatWhenTimestampFormatEmpty() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter("", null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12 00:00:00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTimestampFormatInvalid() {
        new ZonedDateTimeConverter("INVALID", null);
    }

    @Test
    public void shouldShowDefaultFormatWhenTimestampFormatValid() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, "Europe/Rome");

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12 01:00:00");
    }

    @Test
    public void shouldShowSystemDefaultWhenZoneIdNull() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, null);

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12 00:00:00");
    }

    @Test
    public void shouldShowSystemDefaultWhenZoneIdEmpty() {
        ZonedDateTimeConverter zonedDateTimeConverter
                = new ZonedDateTimeConverter(null, "");

        LocalDateTime localDateTime =
                LocalDateTime.of(2020, 12, 12, 0, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        String convertedDateTime = zonedDateTimeConverter.convert(localDateTime);

        assertThat(convertedDateTime).isNotNull();
        assertThat(convertedDateTime).isEqualTo("2020-12-12 00:00:00");
    }

    @Test(expected = ZoneRulesException.class)
    public void shouldThrowExceptionIfZoneIdInvalid() {
        new ZonedDateTimeConverter(null, "INVALID");
    }

}
