package com.guardianai.data.emergency;

import android.content.Context;
import android.util.Log;

import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.models.EmergencyEvaluationRequestDto;
import com.guardianai.data.models.EmergencyEvaluationResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyResponseEngine {

    private static final String TAG = "EmergencyResponseEngine";

    public interface EvaluationCallback {
        void onSuccess(EmergencyEvaluationResponseDto response);
        void onError(String errorMessage);
    }

    private final GuardianApiService apiService;

    public EmergencyResponseEngine(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void evaluateEmergency(EmergencyEvaluationRequestDto request, EvaluationCallback callback) {
        Log.d(TAG, "Evaluating emergency for trigger: " + request.getTriggerSource());
        apiService.evaluateEmergency(request).enqueue(new Callback<EmergencyEvaluationResponseDto>() {
            @Override
            public void onResponse(Call<EmergencyEvaluationResponseDto> call, Response<EmergencyEvaluationResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmergencyEvaluationResponseDto eval = response.body();
                    Log.i(TAG, "Evaluation received: IncidentLevel=" + eval.getIncidentLevel() +
                            ", Action=" + eval.getActionRecommended() +
                            ", Reason=" + eval.getReason());
                    if (callback != null) {
                        callback.onSuccess(eval);
                    }
                } else {
                    String err = "Emergency evaluation failed with status: " + response.code();
                    Log.e(TAG, err);
                    if (callback != null) {
                        callback.onError(err);
                    }
                }
            }

            @Override
            public void onFailure(Call<EmergencyEvaluationResponseDto> call, Throwable t) {
                Log.e(TAG, "Network error during emergency evaluation", t);
                if (callback != null) {
                    callback.onError(t.getMessage());
                }
            }
        });
    }
}
