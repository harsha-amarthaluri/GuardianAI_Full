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

        if (!onboardingCompleted) {
            navigateTo(OnboardingActivity.class);
        } else if (tokenManager.hasToken()) {
            // Validate token against backend if online
            new com.guardianai.data.repository.UserRepository(this).getUserProfile(new com.guardianai.data.repository.UserRepository.ApiCallback<com.guardianai.data.models.UserDto>() {
                @Override
                public void onSuccess(com.guardianai.data.models.UserDto result) {
                    navigateTo(MainActivity.class);
                }

                @Override
                public void onError(String errorMessage, int statusCode) {
                    if (statusCode == 401) {
                        // Token expired or invalid -> clear and force login
                        tokenManager.clearToken();
                        navigateTo(LoginActivity.class);
                    } else {
                        // Network offline / backend temporary unreachable -> allow offline main activity
                        navigateTo(MainActivity.class);
                    }
                }
            });
        } else {
            navigateTo(LoginActivity.class);
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(SplashActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(com.guardianai.R.anim.fade_in, com.guardianai.R.anim.fade_out);
        finish();
    }
}
