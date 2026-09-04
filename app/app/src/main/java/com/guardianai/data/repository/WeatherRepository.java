package com.guardianai.data.repository;

import android.content.Context;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.EnvironmentalRiskDto;
import com.guardianai.data.models.RouteAnalysisRequestDto;
import com.guardianai.data.models.RouteAnalysisResponseDto;
import com.guardianai.data.models.WeatherDataDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private final GuardianApiService apiService;

    public WeatherRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public void getWeather(double latitude, double longitude, ApiCallback<WeatherDataDto> callback) {
        apiService.getWeather(latitude, longitude).enqueue(new Callback<WeatherDataDto>() {
            @Override
            public void onResponse(Call<WeatherDataDto> call, Response<WeatherDataDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load weather: HTTP " + response.code(), response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherDataDto> call, Throwable t) {
                callback.onError("Network error: " + t.getLocalizedMessage(), -1);
            }
        });
    }

    public void getEnvironmentalRisk(double latitude, double longitude, ApiCallback<EnvironmentalRiskDto> callback) {
        apiService.getEnvironmentalRisk(latitude, longitude).enqueue(new Callback<EnvironmentalRiskDto>() {
            @Override
            public void onResponse(Call<EnvironmentalRiskDto> call, Response<EnvironmentalRiskDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load environmental risk: HTTP " + response.code(), response.code());
                }
            }

            @Override
            public void onFailure(Call<EnvironmentalRiskDto> call, Throwable t) {
                callback.onError("Network error: " + t.getLocalizedMessage(), -1);
            }
        });
    }

    public void analyzeRoute(RouteAnalysisRequestDto request, ApiCallback<RouteAnalysisResponseDto> callback) {
        apiService.analyzeRoute(request).enqueue(new Callback<RouteAnalysisResponseDto>() {
            @Override
            public void onResponse(Call<RouteAnalysisResponseDto> call, Response<RouteAnalysisResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Route analysis failed: HTTP " + response.code(), response.code());
                }
            }

            @Override
            public void onFailure(Call<RouteAnalysisResponseDto> call, Throwable t) {
                callback.onError("Network error: " + t.getLocalizedMessage(), -1);
            }
        });
    }
}
