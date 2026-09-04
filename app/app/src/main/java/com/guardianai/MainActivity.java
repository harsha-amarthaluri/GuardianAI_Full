package com.guardianai;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Guardian AI Main Activity Shell (Phase 0 Baseline)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GuardianAI_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Guardian AI MainActivity initialized successfully.");

        TextView textStatus = findViewById(R.id.textTrackingStatus);
        if (textStatus != null) {
            textStatus.setText("Status: System Initialized");
        }
    }
}
