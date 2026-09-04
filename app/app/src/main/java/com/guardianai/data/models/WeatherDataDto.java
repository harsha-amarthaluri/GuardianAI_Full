package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class WeatherDataDto {

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("feels_like")
    private Double feelsLike;

    @SerializedName("humidity")
    private double humidity;

    @SerializedName("wind_speed")
    private double windSpeed;

    @SerializedName("visibility")
    private double visibility;

    @SerializedName("weather_condition")
    private String weatherCondition;

    @SerializedName("precipitation")
    private double precipitation;

    @SerializedName("timestamp")
    private String timestamp;

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getTemperature() { return temperature; }
    public Double getFeelsLike() { return feelsLike; }
    public double getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getVisibility() { return visibility; }
    public String getWeatherCondition() { return weatherCondition; }
    public double getPrecipitation() { return precipitation; }
    public String getTimestamp() { return timestamp; }
}
