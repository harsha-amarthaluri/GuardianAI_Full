package com.guardianai;

import com.google.gson.Gson;
import com.guardianai.data.models.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelParsingTest {

    private final Gson gson = new Gson();

    @Test
    public void testUserDtoParsing() {
        String json = "{"
                + "\"id\":\"u_123\","
                + "\"full_name\":\"Jane Doe\","
                + "\"email\":\"jane@example.com\","
                + "\"phone_number\":\"+1234567890\","
                + "\"role\":\"USER\","
                + "\"is_active\":true,"
                + "\"is_verified\":false,"
                + "\"created_at\":\"2026-08-25T10:00:00Z\""
                + "}";

        UserDto dto = gson.fromJson(json, UserDto.class);
        assertNotNull(dto);
        assertEquals("u_123", dto.getId());
        assertEquals("Jane Doe", dto.getFullName());
        assertEquals("jane@example.com", dto.getEmail());
        assertTrue(dto.isActive());
        assertFalse(dto.isVerified());
    }

    @Test
    public void testGuardianDtoParsing() {
        String json = "{"
                + "\"id\":\"g_456\","
                + "\"user_id\":\"u_123\","
                + "\"name\":\"John Guardian\","
                + "\"phone\":\"+1987654321\","
                + "\"email\":\"john@example.com\","
                + "\"relationship\":\"Parent\","
                + "\"notification_enabled\":true,"
                + "\"created_at\":\"2026-08-25T10:00:00Z\","
                + "\"updated_at\":\"2026-08-25T10:00:00Z\""
                + "}";

        GuardianDto dto = gson.fromJson(json, GuardianDto.class);
        assertNotNull(dto);
        assertEquals("g_456", dto.getId());
        assertEquals("John Guardian", dto.getName());
        assertEquals("Parent", dto.getRelationship());
        assertTrue(dto.isNotificationEnabled());
    }

    @Test
    public void testSafetyScoreResponseParsing() {
        String json = "{"
                + "\"score\":85.0,"
                + "\"category\":\"LOW\","
                + "\"location\":{\"latitude\":37.7749,\"longitude\":-122.4194},"
                + "\"factors\":[],"
                + "\"disclaimer\":\"Safety score is an estimate.\""
                + "}";

        SafetyScoreResponseDto dto = gson.fromJson(json, SafetyScoreResponseDto.class);
        assertNotNull(dto);
        assertEquals(85.0f, dto.getScore(), 0.01f);
        assertEquals("LOW", dto.getCategory());
        assertEquals(37.7749, dto.getLocation().getLatitude(), 0.0001);
        assertEquals("Safety score is an estimate.", dto.getDisclaimer());
    }

    @Test
    public void testSOSResponseParsing() {
        String json = "{"
                + "\"id\":\"sos_789\","
                + "\"user_id\":\"u_123\","
                + "\"latitude\":37.7749,"
                + "\"longitude\":-122.4194,"
                + "\"trigger_type\":\"MANUAL\","
                + "\"status\":\"ALERTING\","
                + "\"created_at\":\"2026-08-25T10:00:00Z\","
                + "\"updated_at\":\"2026-08-25T10:00:00Z\""
                + "}";

        SOSResponseDto dto = gson.fromJson(json, SOSResponseDto.class);
        assertNotNull(dto);
        assertEquals("sos_789", dto.getId());
        assertEquals("MANUAL", dto.getTriggerType());
        assertEquals("ALERTING", dto.getStatus());
    }
}
