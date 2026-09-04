package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class LocationResponseDto {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("accuracy")
    private Float accuracy;

    @SerializedName("timestamp")
    private String timestamp;

    public int getId() { return id; }
    public String getUserId() { return userId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Float getAccuracy() { return accuracy; }
    public String getTimestamp() { return timestamp; }
}
