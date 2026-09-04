package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.SafetyScoreResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafetyRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public SafetyRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void getSafetyScore(double latitude, double longitude, ApiCallback<SafetyScoreResponseDto> callback) {
        apiService.getSafetyScore(latitude, longitude).enqueue(new Callback<SafetyScoreResponseDto>() {
            @Override
            public void onResponse(Call<SafetyScoreResponseDto> call, Response<SafetyScoreResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<SafetyScoreResponseDto> call, Throwable t) {
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
