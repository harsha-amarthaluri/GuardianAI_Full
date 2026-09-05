package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class EmergencyEvaluationRequestDto {
    @SerializedName("trigger_source")
    private String triggerSource;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("battery_level")
    private Float batteryLevel;

    @SerializedName("voice_confidence")
    private Float voiceConfidence;

    @SerializedName("sensor_magnitude")
    private Float sensorMagnitude;

    @SerializedName("heart_rate_bpm")
    private Integer heartRateBpm;

    public EmergencyEvaluationRequestDto(String triggerSource, double latitude, double longitude) {
        this.triggerSource = triggerSource;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Float getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Float batteryLevel) { this.batteryLevel = batteryLevel; }

    public Float getVoiceConfidence() { return voiceConfidence; }
    public void setVoiceConfidence(Float voiceConfidence) { this.voiceConfidence = voiceConfidence; }

    public Float getSensorMagnitude() { return sensorMagnitude; }
    public void setSensorMagnitude(Float sensorMagnitude) { this.sensorMagnitude = sensorMagnitude; }

    public Integer getHeartRateBpm() { return heartRateBpm; }
    public void setHeartRateBpm(Integer heartRateBpm) { this.heartRateBpm = heartRateBpm; }
}
