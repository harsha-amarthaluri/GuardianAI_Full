package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SafetyScoreResponseDto {
    @SerializedName("score")
    private float score;

    @SerializedName("category")
    private String category;

    @SerializedName("location")
    private LocationPointDto location;

    @SerializedName("factors")
    private List<FactorDetailDto> factors;

    @SerializedName("disclaimer")
    private String disclaimer;

    public float getScore() { return score; }
    public String getCategory() { return category; }
    public LocationPointDto getLocation() { return location; }
    public List<FactorDetailDto> getFactors() { return factors; }
    public String getDisclaimer() { return disclaimer; }

    public static class FactorDetailDto {
        @SerializedName("factor")
        private String factor;

        @SerializedName("impact")
        private String impact;

        @SerializedName("weight")
        private float weight;

        public String getFactor() { return factor; }
        public String getImpact() { return impact; }
        public float getWeight() { return weight; }
    }

    public static class LocationPointDto {
        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
