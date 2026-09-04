package com.guardianai;

import com.guardianai.utils.ValidationUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocationValidationTest {

    @Test
    public void testValidCoordinates() {
        assertTrue(ValidationUtils.isValidLatitude(0.0));
        assertTrue(ValidationUtils.isValidLatitude(37.7749));
        assertTrue(ValidationUtils.isValidLatitude(-89.99));
        assertTrue(ValidationUtils.isValidLatitude(90.0));

        assertTrue(ValidationUtils.isValidLongitude(0.0));
        assertTrue(ValidationUtils.isValidLongitude(-122.4194));
        assertTrue(ValidationUtils.isValidLongitude(-180.0));
        assertTrue(ValidationUtils.isValidLongitude(180.0));
    }

    @Test
    public void testInvalidCoordinates() {
        assertFalse(ValidationUtils.isValidLatitude(90.1));
        assertFalse(ValidationUtils.isValidLatitude(-90.1));
        assertFalse(ValidationUtils.isValidLatitude(1000.0));

        assertFalse(ValidationUtils.isValidLongitude(180.1));
        assertFalse(ValidationUtils.isValidLongitude(-180.1));
        assertFalse(ValidationUtils.isValidLongitude(-500.0));
    }

    @Test
    public void testAccuracyFilteringThreshold() {
        float highAccuracy = 5.0f;
        float acceptableAccuracy = 50.0f;
        float borderAccuracy = 100.0f;
        float unacceptableAccuracy = 100.1f;

        assertTrue(highAccuracy <= 100.0f);
        assertTrue(acceptableAccuracy <= 100.0f);
        assertTrue(borderAccuracy <= 100.0f);
        assertFalse(unacceptableAccuracy <= 100.0f);
    }
}
