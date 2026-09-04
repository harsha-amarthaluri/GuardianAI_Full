package com.guardianai.data.api;

import android.content.Context;

import com.guardianai.auth.SessionManager;
import com.guardianai.auth.TokenManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class ApiClient {

    private static final String DEFAULT_BASE_URL = "https://gai-backend-ylzf.onrender.com/";
    private static String currentBaseUrl = DEFAULT_BASE_URL;

    private static Retrofit retrofit;

    public static void setBaseUrl(String baseUrl) {
        currentBaseUrl = baseUrl;
        retrofit = null; // Rebuild retrofit client
    }

    public static String getBaseUrl() {
        return currentBaseUrl;
    }

    public static synchronized GuardianApiService getApiService(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request originalRequest = chain.request();
                            Request.Builder builder = originalRequest.newBuilder();

                            // Attach Bearer JWT if available
                            if (context != null) {
                                String token = TokenManager.getInstance(context).getToken();
                                if (token != null && !token.trim().isEmpty()) {
                                    builder.header("Authorization", "Bearer " + token);
                                }
                            }

                            Request request = builder.build();
                            Response response = chain.proceed(request);

                            // Centralized HTTP 401 Unauthorized handling outside Activities
                            if (response.code() == 401 && context != null) {
                                SessionManager.handleSessionExpired(context);
                            }

                            return response;
                        }
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(GuardianApiService.class);
    }
}
