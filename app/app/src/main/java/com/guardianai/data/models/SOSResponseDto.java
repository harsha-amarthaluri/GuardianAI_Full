package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SOSResponseDto {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("trigger_type")
    private String triggerType;

    @SerializedName("risk_score")
    private Float riskScore;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("resolved_at")
    private String resolvedAt;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTriggerType() { return triggerType; }
    public Float getRiskScore() { return riskScore; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getResolvedAt() { return resolvedAt; }

    public static class SOSListResponseDto {
        @SerializedName("items")
        private List<SOSResponseDto> items;

        @SerializedName("total")
        private int total;

        @SerializedName("skip")
        private int skip;

        @SerializedName("limit")
        private int limit;

        public List<SOSResponseDto> getItems() { return items; }
        public int getTotal() { return total; }
        public int getSkip() { return skip; }
        public int getLimit() { return limit; }
    }
}
