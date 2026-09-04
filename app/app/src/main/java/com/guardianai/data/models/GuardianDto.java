package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class GuardianDto {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("relationship")
    private String relationship;

    @SerializedName("notification_enabled")
    private boolean notificationEnabled;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRelationship() { return relationship; }
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
