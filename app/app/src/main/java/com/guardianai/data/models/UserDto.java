package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    @SerializedName("id")
    private String id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("role")
    private String role;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }
    public boolean isVerified() { return isVerified; }
    public String getCreatedAt() { return createdAt; }
}
