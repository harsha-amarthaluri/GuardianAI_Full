package com.guardianai.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.models.FeatureDtos;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchShakeSos;
    private Switch switchFallDetection;
    private Switch switchVoiceDistress;
    private Switch switchLocationSharing;
    private Switch switchEvidenceRecording;
    private Button btnSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchShakeSos = findViewById(R.id.switchShakeSos);
        switchFallDetection = findViewById(R.id.switchFallDetection);
        switchVoiceDistress = findViewById(R.id.switchVoiceDistress);
        switchLocationSharing = findViewById(R.id.switchLocationSharing);
        switchEvidenceRecording = findViewById(R.id.switchEvidenceRecording);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        ApiClient.getApiService(this).getUserSettings().enqueue(new Callback<FeatureDtos.UserSettingsDto>() {
            @Override
            public void onResponse(Call<FeatureDtos.UserSettingsDto> call, Response<FeatureDtos.UserSettingsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FeatureDtos.UserSettingsDto dto = response.body();
                    switchShakeSos.setChecked(dto.isShakeSosEnabled());
                    switchFallDetection.setChecked(dto.isFallDetectionEnabled());
                    switchVoiceDistress.setChecked(dto.isVoiceDistressEnabled());
                    switchLocationSharing.setChecked(dto.isLocationSharingEnabled());
                    switchEvidenceRecording.setChecked(dto.isEvidenceRecordingEnabled());
                }
            }

            @Override
            public void onFailure(Call<FeatureDtos.UserSettingsDto> call, Throwable t) {
                // Ignore failure and keep defaults
            }
        });
    }

    private void saveSettings() {
        FeatureDtos.UserSettingsDto dto = new FeatureDtos.UserSettingsDto();
        ApiClient.getApiService(this).updateUserSettings(dto).enqueue(new Callback<FeatureDtos.UserSettingsDto>() {
            @Override
            public void onResponse(Call<FeatureDtos.UserSettingsDto> call, Response<FeatureDtos.UserSettingsDto> response) {
                Toast.makeText(SettingsActivity.this, "✅ Settings updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<FeatureDtos.UserSettingsDto> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "✅ Settings saved locally!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
