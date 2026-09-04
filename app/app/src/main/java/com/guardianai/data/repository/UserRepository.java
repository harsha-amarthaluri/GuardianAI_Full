package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.UserDto;
import com.guardianai.data.models.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public UserRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void getUserProfile(ApiCallback<UserDto> callback) {
        apiService.getUserProfile().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void updateUserProfile(String fullName, String phoneNumber, ApiCallback<UserDto> callback) {
        UserUpdateRequest request = new UserUpdateRequest(fullName, phoneNumber);
        apiService.updateUserProfile(request).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void deleteAccount(ApiCallback<Void> callback) {
        apiService.deleteUserProfile().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception ignored) {}
        return "HTTP Error " + response.code();
    }
}
