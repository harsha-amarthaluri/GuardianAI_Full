package com.guardianai.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;
import com.guardianai.ui.auth.LoginActivity;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREF_ONBOARDING_COMPLETE = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> completeOnboardingAndNavigate());
    }

    private void completeOnboardingAndNavigate() {
        SharedPreferences prefs = getSharedPreferences("guardian_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_ONBOARDING_COMPLETE, true).apply();

        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
