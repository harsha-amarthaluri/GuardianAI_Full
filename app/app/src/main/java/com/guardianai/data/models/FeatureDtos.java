package com.guardianai.data.models;

import java.util.List;

public class FeatureDtos {

    // Chat DTOs
    public static class ChatRequestDto {
        private String message;
        private Double latitude;
        private Double longitude;

        public ChatRequestDto(String message) {
            this.message = message;
        }

        public ChatRequestDto(String message, Double latitude, Double longitude) {
            this.message = message;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getMessage() { return message; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    public static class ChatResponseDto {
        private String reply;
        private boolean is_emergency_detected;
        private List<String> suggested_actions;
        private String timestamp;

        public String getReply() { return reply; }
        public boolean isEmergencyDetected() { return is_emergency_detected; }
        public List<String> getSuggestedActions() { return suggested_actions; }
        public String getTimestamp() { return timestamp; }
    }

    // CheckIn DTOs
    public static class CheckInRequestDto {
        private int duration_minutes;
        private String destination;
        private String note;

        public CheckInRequestDto(int duration_minutes, String destination, String note) {
            this.duration_minutes = duration_minutes;
            this.destination = destination;
            this.note = note;
        }

        public int getDurationMinutes() { return duration_minutes; }
        public String getDestination() { return destination; }
        public String getNote() { return note; }
    }

    public static class CheckInResponseDto {
        private String id;
        private String status;
        private int duration_minutes;
        private String started_at;
        private String expires_at;
        private String destination;

        public String getId() { return id; }
        public String getStatus() { return status; }
        public int getDurationMinutes() { return duration_minutes; }
        public String getStartedAt() { return started_at; }
        public String getExpiresAt() { return expires_at; }
        public String getDestination() { return destination; }
    }

    // Safe Places DTOs
    public static class SafePlaceDto {
        private String id;
        private String name;
        private String category;
        private double latitude;
        private double longitude;
        private String address;
        private double distance_meters;
        private String phone;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getAddress() { return address; }
        public double getDistanceMeters() { return distance_meters; }
        public String getPhone() { return phone; }
    }

    public static class SafePlacesResponseDto {
        private double latitude;
        private double longitude;
        private int total_found;
        private List<SafePlaceDto> places;

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public int getTotalFound() { return total_found; }
        public List<SafePlaceDto> getPlaces() { return places; }
    }

    // User Settings DTO
    public static class UserSettingsDto {
        private boolean shake_sos_enabled;
        private boolean fall_detection_enabled;
        private boolean voice_distress_enabled;
        private boolean motion_detection_enabled;
        private boolean evidence_recording_enabled;
        private boolean location_sharing_enabled;
        private boolean dark_mode_enabled;

        public boolean isShakeSosEnabled() { return shake_sos_enabled; }
        public boolean isFallDetectionEnabled() { return fall_detection_enabled; }
        public boolean isVoiceDistressEnabled() { return voice_distress_enabled; }
        public boolean isMotionDetectionEnabled() { return motion_detection_enabled; }
        public boolean isEvidenceRecordingEnabled() { return evidence_recording_enabled; }
        public boolean isLocationSharingEnabled() { return location_sharing_enabled; }
        public boolean isDarkModeEnabled() { return dark_mode_enabled; }
    }
}
