package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class GuardianUpdateRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("relationship")
    private String relationship;

    @SerializedName("notification_enabled")
    private Boolean notificationEnabled;

    public GuardianUpdateRequest(String name, String phone, String email, String relationship, Boolean notificationEnabled) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.relationship = relationship;
        this.notificationEnabled = notificationEnabled;
    }
}
