package com.guardianai.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;

public class SafetySetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_setup);

        RadioGroup groupRisk = findViewById(R.id.groupRiskThreshold);
        CheckBox checkAutoShare = findViewById(R.id.checkAutoShareSOS);
        CheckBox checkLowBattery = findViewById(R.id.checkLowBatteryAlert);
        Button btnFinish = findViewById(R.id.btnFinishSetup);

        btnFinish.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("guardian_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("safety_setup_completed", true)
                    .putBoolean("auto_share_sos", checkAutoShare.isChecked())
                    .putBoolean("low_battery_alert", checkLowBattery.isChecked())
                    .apply();

            Toast.makeText(this, "Safety configuration saved.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SafetySetupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
