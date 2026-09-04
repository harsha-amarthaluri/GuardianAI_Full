package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class LocationRequestDto {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("accuracy")
    private Float accuracy;

    public LocationRequestDto(double latitude, double longitude, Float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
}
