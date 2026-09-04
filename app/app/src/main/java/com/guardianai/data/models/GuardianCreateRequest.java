package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class GuardianCreateRequest {
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

    public GuardianCreateRequest(String name, String phone, String email, String relationship, boolean notificationEnabled) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.relationship = relationship;
        this.notificationEnabled = notificationEnabled;
    }
}
