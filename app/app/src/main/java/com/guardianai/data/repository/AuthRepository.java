package com.guardianai.data.repository;

import android.content.Context;

import com.guardianai.auth.TokenManager;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final GuardianApiService apiService;
    private final TokenManager tokenManager;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public AuthRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
        this.tokenManager = TokenManager.getInstance(context);
    }

    public void register(UserRegisterRequest request, ApiCallback<UserDto> callback) {
        apiService.register(request).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Registration failed: " + parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                callback.onError("Network connection failure: " + t.getMessage(), 0);
            }
        });
    }

    public void login(String email, String password, ApiCallback<TokenResponseDto> callback) {
        UserLoginRequest request = new UserLoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<TokenResponseDto>() {
            @Override
            public void onResponse(Call<TokenResponseDto> call, Response<TokenResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getAccessToken(), email);
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Login failed: " + parseError(response), response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponseDto> call, Throwable t) {
                callback.onError("Network connection failure: " + t.getMessage(), 0);
            }
        });
    }

    public void getCurrentUser(ApiCallback<UserDto> callback) {
        apiService.getCurrentUserAuth().enqueue(new Callback<UserDto>() {
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
                callback.onError("Network connection failure: " + t.getMessage(), 0);
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
