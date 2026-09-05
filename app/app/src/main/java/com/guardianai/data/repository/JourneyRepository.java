package com.guardianai.data.repository;

import android.content.Context;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.JourneyDto;
import com.guardianai.data.models.JourneyStartRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public JourneyRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void startJourney(JourneyStartRequestDto request, ApiCallback<JourneyDto> callback) {
        apiService.startJourney(request).enqueue(new Callback<JourneyDto>() {
            @Override
            public void onResponse(Call<JourneyDto> call, Response<JourneyDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to start journey. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JourneyDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getActiveJourney(ApiCallback<JourneyDto> callback) {
        apiService.getActiveJourney().enqueue(new Callback<JourneyDto>() {
            @Override
            public void onResponse(Call<JourneyDto> call, Response<JourneyDto> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get active journey. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JourneyDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void completeJourney(String journeyId, ApiCallback<JourneyDto> callback) {
        apiService.completeJourney(journeyId).enqueue(new Callback<JourneyDto>() {
            @Override
            public void onResponse(Call<JourneyDto> call, Response<JourneyDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to complete journey. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JourneyDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
