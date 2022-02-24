package uk.gov.digital.ho.hocs.audit.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.UUID;

public class UuidStringCheckerTest {

    @Test
    public void shouldReturnTrueWithUppercaseUuidString() {
        Assert.assertTrue(UuidStringChecker.isUUID(UUID.randomUUID().toString().toUpperCase(Locale.ROOT)));
    }

    @Test
    public void shouldReturnTrueWithLowercaseUuidString() {
        Assert.assertTrue(UuidStringChecker.isUUID(UUID.randomUUID().toString().toLowerCase(Locale.ROOT)));
    }

    @Test
    public void shouldReturnFalseWithNullString() {
        Assert.assertFalse(UuidStringChecker.isUUID(null));
    }

    @Test
    public void shouldReturnFalseWithEmptyString() {
        Assert.assertFalse(UuidStringChecker.isUUID(""));
    }

    @Test
    public void shouldReturnFalseWithInvalidString() {
        Assert.assertFalse(UuidStringChecker.isUUID("TEST"));
    }

}
