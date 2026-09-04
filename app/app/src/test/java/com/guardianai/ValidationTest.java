package com.guardianai;

import com.guardianai.utils.ValidationUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("jane.doe+test@domain.co.uk"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("not_an_email"));
        assertFalse(ValidationUtils.isValidEmail("user@"));
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(ValidationUtils.isValidPassword("12345678"));
        assertTrue(ValidationUtils.isValidPassword("SecurePassword123!"));
        assertFalse(ValidationUtils.isValidPassword("short"));
        assertFalse(ValidationUtils.isValidPassword(""));
    }

    @Test
    public void testPasswordMatch() {
        assertTrue(ValidationUtils.isPasswordMatch("Pass1234", "Pass1234"));
        assertFalse(ValidationUtils.isPasswordMatch("Pass1234", "Different1234"));
    }

    @Test
    public void testCoordinateBoundsValidation() {
        assertTrue(ValidationUtils.isValidLatitude(37.7749));
        assertTrue(ValidationUtils.isValidLatitude(-90.0));
        assertTrue(ValidationUtils.isValidLatitude(90.0));
        assertFalse(ValidationUtils.isValidLatitude(90.1));
        assertFalse(ValidationUtils.isValidLatitude(-91.0));

        assertTrue(ValidationUtils.isValidLongitude(-122.4194));
        assertTrue(ValidationUtils.isValidLongitude(-180.0));
        assertTrue(ValidationUtils.isValidLongitude(180.0));
        assertFalse(ValidationUtils.isValidLongitude(180.1));
        assertFalse(ValidationUtils.isValidLongitude(-181.0));
    }
}
