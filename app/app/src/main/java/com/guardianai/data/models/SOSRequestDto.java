package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class SOSRequestDto {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("trigger_type")
    private String triggerType;

    public SOSRequestDto(double latitude, double longitude, String triggerType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.triggerType = triggerType;
    }
}
