package com.guardianai.data.api;

import com.guardianai.data.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface GuardianApiService {

    // Authentication Endpoints
    @POST("api/v1/auth/register")
    Call<UserDto> register(@Body UserRegisterRequest request);

    @POST("api/v1/auth/login")
    Call<TokenResponseDto> login(@Body UserLoginRequest request);

    @GET("api/v1/auth/me")
    Call<UserDto> getCurrentUserAuth();

    // User Profile Endpoints
    @GET("api/v1/users/me")
    Call<UserDto> getUserProfile();

    @PUT("api/v1/users/me")
    Call<UserDto> updateUserProfile(@Body UserUpdateRequest request);

    @DELETE("api/v1/users/me")
    Call<Void> deleteUserProfile();

    // Guardian Network Endpoints
    @POST("api/v1/guardians")
    Call<GuardianDto> createGuardian(@Body GuardianCreateRequest request);

    @GET("api/v1/guardians")
    Call<List<GuardianDto>> listGuardians();

    @GET("api/v1/guardians/{id}")
    Call<GuardianDto> getGuardian(@Path("id") String guardianId);

    @PUT("api/v1/guardians/{id}")
    Call<GuardianDto> updateGuardian(@Path("id") String guardianId, @Body GuardianUpdateRequest request);

    @DELETE("api/v1/guardians/{id}")
    Call<Void> deleteGuardian(@Path("id") String guardianId);

    // Location Telemetry Endpoints
    @POST("api/v1/locations")
    Call<LocationResponseDto> recordLocation(@Body LocationRequestDto request);

    @POST("api/v1/locations/batch")
    Call<LocationBatchResponse> recordLocationBatch(@Body LocationBatchCreateRequest request);

    @POST("api/v1/locations/batch")
    Call<LocationBatchResponse> recordLocationBatchSync(@Body LocationBatchCreateRequest request);

    // Safety Score Endpoint
    @GET("api/v1/safety-score")
    Call<SafetyScoreResponseDto> getSafetyScore(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @POST("api/v1/safety-score/route-analysis")
    Call<RouteAnalysisResponseDto> analyzeRoute(@Body RouteAnalysisRequestDto request);

    // Weather & Environmental Risk Endpoints
    @GET("api/v1/weather")
    Call<WeatherDataDto> getWeather(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @GET("api/v1/environmental-risk")
    Call<EnvironmentalRiskDto> getEnvironmentalRisk(@Query("latitude") double latitude, @Query("longitude") double longitude);

    // Threat Awareness Endpoints
    @GET("api/v1/threats")
    Call<ThreatDto.ThreatListResponseDto> getThreats(
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("radius") Double radius,
            @Query("category") String category
    );

    // SOS Emergency Endpoints
    @POST("api/v1/sos")
    Call<SOSResponseDto> createSOS(@Body SOSRequestDto request);

    @GET("api/v1/sos")
    Call<SOSResponseDto.SOSListResponseDto> listSOSIncidents(@Query("skip") int skip, @Query("limit") int limit);

    @GET("api/v1/sos/{sos_id}/events")
    Call<List<SOSEventResponseDto>> getSOSEvents(@Path("sos_id") String sosId);

    // Emergency Response Decision Engine
    @POST("api/v1/emergency/evaluate")
    Call<EmergencyEvaluationResponseDto> evaluateEmergency(@Body EmergencyEvaluationRequestDto request);

    // Notification FCM Registration
    @POST("api/v1/notifications/token")
    Call<Void> registerFcmToken(@Body FCMTokenRequestDto request);

    // Safe Journeys Endpoints
    @POST("api/v1/journeys/start")
    Call<JourneyDto> startJourney(@Body JourneyStartRequestDto request);

    @GET("api/v1/journeys/active")
    Call<JourneyDto> getActiveJourney();

    @POST("api/v1/journeys/{id}/complete")
    Call<JourneyDto> completeJourney(@Path("id") String journeyId);
}
