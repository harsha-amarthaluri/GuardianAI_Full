package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EnvironmentalRiskDto {

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("risk_score")
    private double riskScore;

    @SerializedName("risk_factors")
    private List<EnvironmentalFactorDto> riskFactors;

    @SerializedName("weather")
    private WeatherDataDto weather;

    @SerializedName("timestamp")
    private String timestamp;

    public String getRiskLevel() { return riskLevel; }
    public double getRiskScore() { return riskScore; }
    public List<EnvironmentalFactorDto> getRiskFactors() { return riskFactors; }
    public WeatherDataDto getWeather() { return weather; }
    public String getTimestamp() { return timestamp; }

    public static class EnvironmentalFactorDto {
        @SerializedName("factor")
        private String factor;

        @SerializedName("impact")
        private String impact;

        @SerializedName("weight")
        private double weight;

        public String getFactor() { return factor; }
        public String getImpact() { return impact; }
        public double getWeight() { return weight; }
    }
}
