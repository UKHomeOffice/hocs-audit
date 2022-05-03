package uk.gov.digital.ho.hocs.audit.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeAttributeConverterTest {

    private LocalDateTimeAttributeConverter converter;

    @BeforeEach
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

        Assertions.assertNotNull(convertedDate);
        Assertions.assertInstanceOf(Timestamp.class, convertedDate);
        Assertions.assertEquals("2018-01-01 00:01:00.0", convertedDate.toString());
    }

    @Test
    public void shouldConvertToEntityAttribute() {
        Timestamp date = new Timestamp(1514764860000L);
        LocalDateTime convertedDate = converter.convertToEntityAttribute(date);

        Assertions.assertNotNull(convertedDate);
        Assertions.assertInstanceOf(LocalDateTime.class, convertedDate);
        Assertions.assertEquals("2018-01-01T00:01", convertedDate.toString());
    }

}
