package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.LocationRequestDto;
import com.guardianai.data.models.LocationResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public LocationRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void submitLocation(double latitude, double longitude, Float accuracy, ApiCallback<LocationResponseDto> callback) {
        LocationRequestDto request = new LocationRequestDto(latitude, longitude, accuracy);
        apiService.recordLocation(request).enqueue(new Callback<LocationResponseDto>() {
            @Override
            public void onResponse(Call<LocationResponseDto> call, Response<LocationResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<LocationResponseDto> call, Throwable t) {
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
