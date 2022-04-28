package uk.gov.digital.ho.hocs.audit.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;

public class UuidStringCheckerTest {

    @Test
    public void shouldReturnTrueWithUppercaseUuidString() {
        Assertions.assertTrue(UuidStringChecker.isUUID(UUID.randomUUID().toString().toUpperCase(Locale.ROOT)));
    }

    @Test
    public void shouldReturnTrueWithLowercaseUuidString() {
        Assertions.assertTrue(UuidStringChecker.isUUID(UUID.randomUUID().toString().toLowerCase(Locale.ROOT)));
    }

    @Test
    public void shouldReturnFalseWithNullString() {
        Assertions.assertFalse(UuidStringChecker.isUUID(null));
    }

    @Test
    public void shouldReturnFalseWithEmptyString() {
        Assertions.assertFalse(UuidStringChecker.isUUID(""));
    }

    @Test
    public void shouldReturnFalseWithInvalidString() {
        Assertions.assertFalse(UuidStringChecker.isUUID("TEST"));
    }

}
