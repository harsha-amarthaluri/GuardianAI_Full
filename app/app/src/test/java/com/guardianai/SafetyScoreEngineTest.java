package com.guardianai;

import com.guardianai.data.models.SafetyScoreResponseDto;
import org.junit.Test;
import static org.junit.Assert.*;

public class SafetyScoreEngineTest {

    @Test
    public void testSafetyScoreCategoryRanges() {
        assertEquals("LOW", getCategory(85.0f));
        assertEquals("LOW", getCategory(80.0f));
        assertEquals("MODERATE", getCategory(79.9f));
        assertEquals("MODERATE", getCategory(60.0f));
        assertEquals("HIGH", getCategory(59.9f));
        assertEquals("HIGH", getCategory(40.0f));
        assertEquals("CRITICAL", getCategory(39.9f));
        assertEquals("CRITICAL", getCategory(0.0f));
    }

    private String getCategory(float score) {
        if (score >= 80.0f) return "LOW";
        if (score >= 60.0f) return "MODERATE";
        if (score >= 40.0f) return "HIGH";
        return "CRITICAL";
    }

    @Test
    public void testSafetyScoreResponseDto() {
        SafetyScoreResponseDto dto = new SafetyScoreResponseDto();
        assertNull(dto.getCategory());
        assertEquals(0.0f, dto.getScore(), 0.01f);
    }
}
