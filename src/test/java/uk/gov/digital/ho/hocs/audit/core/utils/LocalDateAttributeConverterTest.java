package uk.gov.digital.ho.hocs.audit.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

public class LocalDateAttributeConverterTest {

    private LocalDateAttributeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LocalDateAttributeConverter();
    }

    @Test
    public void shouldConvertToDatabaseColumn() {
        LocalDate date = LocalDate.of(2018, 1, 1);

        Date convertedDate = converter.convertToDatabaseColumn(date);

        Assertions.assertNotNull(convertedDate);
        Assertions.assertInstanceOf(Date.class, convertedDate);
        Assertions.assertEquals("2018-01-01", convertedDate.toString());
    }

    @Test
    public void shouldConvertToEntityAttribute() {
        System.out.println();
        Date date = new Date(1514764860000L);
        LocalDate convertedDate = converter.convertToEntityAttribute(date);

        Assertions.assertNotNull(convertedDate);
        Assertions.assertInstanceOf(LocalDate.class, convertedDate);
        Assertions.assertEquals("2018-01-01", convertedDate.toString());
    }

}
