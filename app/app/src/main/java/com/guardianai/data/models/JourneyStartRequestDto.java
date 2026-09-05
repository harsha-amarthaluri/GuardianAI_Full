package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class JourneyStartRequestDto {
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

    public JourneyStartRequestDto(double originLatitude, double originLongitude, String destinationAddress, double destinationLatitude, double destinationLongitude, String expectedArrivalTime) {
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationAddress = destinationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public double getOriginLatitude() { return originLatitude; }
    public double getOriginLongitude() { return originLongitude; }
    public String getDestinationAddress() { return destinationAddress; }
    public double getDestinationLatitude() { return destinationLatitude; }
    public double getDestinationLongitude() { return destinationLongitude; }
    public String getExpectedArrivalTime() { return expectedArrivalTime; }
}
