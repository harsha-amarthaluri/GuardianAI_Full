package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class FCMTokenRequestDto {
    @SerializedName("token")
    private String token;

    @SerializedName("device_type")
    private String deviceType;

    public FCMTokenRequestDto(String token, String deviceType) {
        this.token = token;
        this.deviceType = deviceType;
    }

    public String getToken() { return token; }
    public String getDeviceType() { return deviceType; }
}
