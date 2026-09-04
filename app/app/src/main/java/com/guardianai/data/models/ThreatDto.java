package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ThreatDto {

    @SerializedName("id")
    private String id;

    @SerializedName("category")
    private String category;

    @SerializedName("severity")
    private float severity;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("radius")
    private float radius;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("source")
    private String source;

    @SerializedName("confidence")
    private float confidence;

    @SerializedName("is_active")
    private boolean isActive;

    public String getId() { return id; }
    public String getCategory() { return category; }
    public float getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRadius() { return radius; }
    public String getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    public float getConfidence() { return confidence; }
    public boolean isActive() { return isActive; }

    public static class ThreatListResponseDto {
        @SerializedName("items")
        private List<ThreatDto> items;

        @SerializedName("total")
        private int total;

        public List<ThreatDto> getItems() { return items; }
        public int getTotal() { return total; }
    }
}
