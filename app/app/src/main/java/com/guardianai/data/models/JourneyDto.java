package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class JourneyDto {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("origin_latitude")
    private double originLatitude;

    @SerializedName("origin_longitude")
    private double originLongitude;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("destination_latitude")
    private double destinationLatitude;

    @SerializedName("destination_longitude")
    private double destinationLongitude;

    @SerializedName("expected_arrival_time")
    private String expectedArrivalTime;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getOriginLatitude() { return originLatitude; }
    public double getOriginLongitude() { return originLongitude; }
    public String getDestinationAddress() { return destinationAddress; }
    public double getDestinationLatitude() { return destinationLatitude; }
    public double getDestinationLongitude() { return destinationLongitude; }
    public String getExpectedArrivalTime() { return expectedArrivalTime; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
