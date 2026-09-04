package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteAnalysisResponseDto {

    @SerializedName("destination_name")
    private String destinationName;

    @SerializedName("total_active_threats_in_region")
    private int totalActiveThreatsInRegion;

    @SerializedName("corridors")
    private List<CorridorOptionDto> corridors;

    @SerializedName("recommended_corridor")
    private String recommendedCorridor;

    @SerializedName("timestamp")
    private String timestamp;

    public String getDestinationName() { return destinationName; }
    public int getTotalActiveThreatsInRegion() { return totalActiveThreatsInRegion; }
    public List<CorridorOptionDto> getCorridors() { return corridors; }
    public String getRecommendedCorridor() { return recommendedCorridor; }
    public String getTimestamp() { return timestamp; }

    public static class CorridorOptionDto {
        @SerializedName("corridor_name")
        private String corridorName;

        @SerializedName("distance_km")
        private double distanceKm;

        @SerializedName("nearby_threat_count")
        private int nearbyThreatCount;

        @SerializedName("threat_severity_sum")
        private double threatSeveritySum;

        @SerializedName("risk_level")
        private String riskLevel;

        @SerializedName("description")
        private String description;

        public String getCorridorName() { return corridorName; }
        public double getDistanceKm() { return distanceKm; }
        public int getNearbyThreatCount() { return nearbyThreatCount; }
        public double getThreatSeveritySum() { return threatSeveritySum; }
        public String getRiskLevel() { return riskLevel; }
        public String getDescription() { return description; }
    }
}
