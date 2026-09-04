package com.guardianai.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.guardianai.R;

public class PermissionsActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 201;
    private static final int REQ_NOTIF = 202;

    private Button btnGrantLocationPermission, btnGrantNotificationPermission, btnContinueSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        btnGrantLocationPermission = findViewById(R.id.btnGrantLocationPermission);
        btnGrantNotificationPermission = findViewById(R.id.btnGrantNotificationPermission);
        btnContinueSetup = findViewById(R.id.btnContinueSetup);

        btnGrantLocationPermission.setOnClickListener(v -> requestLocationPermission());
        btnGrantNotificationPermission.setOnClickListener(v -> requestNotificationPermission());
        btnContinueSetup.setOnClickListener(v -> navigateToSafetySetup());

        updatePermissionButtonsState();
    }

    private void updatePermissionButtonsState() {
        boolean locGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (locGranted) {
            btnGrantLocationPermission.setText("✓ Location Permission Granted");
            btnGrantLocationPermission.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean notifGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            if (notifGranted) {
                btnGrantNotificationPermission.setText("✓ Notification Permission Granted");
                btnGrantNotificationPermission.setEnabled(false);
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQ_LOCATION);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.POST_NOTIFICATIONS
            }, REQ_NOTIF);
        } else {
            Toast.makeText(this, "Notification permission granted automatically on this Android version.", Toast.LENGTH_SHORT).show();
            btnGrantNotificationPermission.setText("✓ Notification Access Active");
            btnGrantNotificationPermission.setEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updatePermissionButtonsState();
    }

    private void navigateToSafetySetup() {
        Intent intent = new Intent(PermissionsActivity.this, SafetySetupActivity.class);
        startActivity(intent);
        finish();
    }
}
