package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {
    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone_number")
    private String phoneNumber;

    public UserUpdateRequest(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}
