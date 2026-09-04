package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class RouteAnalysisRequestDto {

    @SerializedName("start_latitude")
    private double startLatitude;

    @SerializedName("start_longitude")
    private double startLongitude;

    @SerializedName("end_latitude")
    private double endLatitude;

    @SerializedName("end_longitude")
    private double endLongitude;

    @SerializedName("destination_name")
    private String destinationName;

    public RouteAnalysisRequestDto(double startLatitude, double startLongitude, double endLatitude, double endLongitude, String destinationName) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.destinationName = destinationName;
    }
}
