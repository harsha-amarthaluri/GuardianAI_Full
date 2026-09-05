package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.SOSRequestDto;
import com.guardianai.data.models.SOSResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SOSRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public SOSRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void triggerSOS(double latitude, double longitude, String triggerType, ApiCallback<SOSResponseDto> callback) {
        SOSRequestDto request = new SOSRequestDto(latitude, longitude, triggerType);
        apiService.createSOS(request).enqueue(new Callback<SOSResponseDto>() {
            @Override
            public void onResponse(Call<SOSResponseDto> call, Response<SOSResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<SOSResponseDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void listSOSIncidents(int skip, int limit, ApiCallback<SOSResponseDto.SOSListResponseDto> callback) {
        apiService.listSOSIncidents(skip, limit).enqueue(new Callback<SOSResponseDto.SOSListResponseDto>() {
            @Override
            public void onResponse(Call<SOSResponseDto.SOSListResponseDto> call, Response<SOSResponseDto.SOSListResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<SOSResponseDto.SOSListResponseDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void getSOSEvents(String sosId, ApiCallback<java.util.List<com.guardianai.data.models.SOSEventResponseDto>> callback) {
        apiService.getSOSEvents(sosId).enqueue(new Callback<java.util.List<com.guardianai.data.models.SOSEventResponseDto>>() {
            @Override
            public void onResponse(Call<java.util.List<com.guardianai.data.models.SOSEventResponseDto>> call, Response<java.util.List<com.guardianai.data.models.SOSEventResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<com.guardianai.data.models.SOSEventResponseDto>> call, Throwable t) {
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
