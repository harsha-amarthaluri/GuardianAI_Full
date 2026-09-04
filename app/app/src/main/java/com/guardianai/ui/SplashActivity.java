package com.guardianai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.auth.SessionManager;
import com.guardianai.auth.TokenManager;
import com.guardianai.ui.auth.LoginActivity;
import com.guardianai.utils.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        // Register centralized session expiration listener across the app
        SessionManager.setSessionListener(new SessionManager.SessionListener() {
            @Override
            public void onSessionExpired() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // 1-second splash delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthAndNavigate();
            }
        }, 1000);
    }

    private void checkAuthAndNavigate() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        boolean onboardingCompleted = getSharedPreferences("guardian_prefs", MODE_PRIVATE)
                .getBoolean("onboarding_completed", false);

        Intent intent;
        if (!onboardingCompleted) {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        } else if (tokenManager.hasToken()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(com.guardianai.R.anim.fade_in, com.guardianai.R.anim.fade_out);
        finish();
    }
}
