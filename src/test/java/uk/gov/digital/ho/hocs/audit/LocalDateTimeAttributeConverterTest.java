package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.application.LocalDateTimeAttributeConverter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class LocalDateTimeAttributeConverterTest {

    private LocalDateTimeAttributeConverter converter;

    @Before
    public void setUp() {
        this.converter = new LocalDateTimeAttributeConverter();
    }

    @Test
    public void shouldConvertToDatabaseColumn() {
        LocalDateTime date = LocalDateTime.of(
                2018,
                1,
                1,
                0,
                1
        );
        Timestamp convertedDate = converter.convertToDatabaseColumn(date);

        assertThat(convertedDate).isNotNull();
        assertThat(convertedDate).isInstanceOf(Timestamp.class);
        assertThat(convertedDate.toString()).isEqualTo("2018-01-01 00:01:00.0");
    }

    @Test
    public void shouldConvertToEntityAttribute() {
        Timestamp date = new Timestamp(1514764860000L);
        LocalDateTime convertedDate = converter.convertToEntityAttribute(date);

        assertThat(convertedDate).isNotNull();
        assertThat(convertedDate).isInstanceOf(LocalDateTime.class);
        assertThat(convertedDate.toString()).isEqualTo("2018-01-01T00:01");
    }

}