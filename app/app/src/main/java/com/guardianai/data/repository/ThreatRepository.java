package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.ThreatDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThreatRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public ThreatRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void getNearbyThreats(Double latitude, Double longitude, Double radius, String category, ApiCallback<ThreatDto.ThreatListResponseDto> callback) {
        apiService.getThreats(latitude, longitude, radius, category).enqueue(new Callback<ThreatDto.ThreatListResponseDto>() {
            @Override
            public void onResponse(Call<ThreatDto.ThreatListResponseDto> call, Response<ThreatDto.ThreatListResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<ThreatDto.ThreatListResponseDto> call, Throwable t) {
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
