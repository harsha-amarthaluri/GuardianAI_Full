package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.GuardianCreateRequest;
import com.guardianai.data.models.GuardianDto;
import com.guardianai.data.models.GuardianUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuardianRepository {

    private final GuardianApiService apiService;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public GuardianRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void listGuardians(ApiCallback<List<GuardianDto>> callback) {
        apiService.listGuardians().enqueue(new Callback<List<GuardianDto>>() {
            @Override
            public void onResponse(Call<List<GuardianDto>> call, Response<List<GuardianDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GuardianDto>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void createGuardian(GuardianCreateRequest request, ApiCallback<GuardianDto> callback) {
        apiService.createGuardian(request).enqueue(new Callback<GuardianDto>() {
            @Override
            public void onResponse(Call<GuardianDto> call, Response<GuardianDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<GuardianDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void updateGuardian(String guardianId, GuardianUpdateRequest request, ApiCallback<GuardianDto> callback) {
        apiService.updateGuardian(guardianId, request).enqueue(new Callback<GuardianDto>() {
            @Override
            public void onResponse(Call<GuardianDto> call, Response<GuardianDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<GuardianDto> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage(), 0);
            }
        });
    }

    public void deleteGuardian(String guardianId, ApiCallback<Void> callback) {
        apiService.deleteGuardian(guardianId).enqueue(new Callback<Void>() {
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
