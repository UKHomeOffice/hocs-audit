package uk.gov.digital.ho.hocs.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.digital.ho.hocs.audit.application.LocalDateAttributeConverter;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class LocalDateAttributeConverterTest {

    private LocalDateAttributeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LocalDateAttributeConverter();
    }

    @Test
    public void shouldConvertToDatabaseColumn() {
        LocalDate date = LocalDate.of(
                2018,
                1,
                1
        );

        Date convertedDate = converter.convertToDatabaseColumn(date);

        assertThat(convertedDate).isNotNull();
        assertThat(convertedDate).isInstanceOf(Date.class);
        assertThat(convertedDate.toString()).isEqualTo("2018-01-01");
    }


    @Test
    public void shouldConvertToEntityAttribute() {
        System.out.println();
        Date date = new Date(1514764860000L);
        LocalDate convertedDate = converter.convertToEntityAttribute(date);

        assertThat(convertedDate).isNotNull();
        assertThat(convertedDate).isInstanceOf(LocalDate.class);
        assertThat(convertedDate.toString()).isEqualTo("2018-01-01");
    }

}
