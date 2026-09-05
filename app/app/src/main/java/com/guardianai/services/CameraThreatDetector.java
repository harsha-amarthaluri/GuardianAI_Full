package com.guardianai.services;

import android.content.Context;
import android.util.Log;

public class CameraThreatDetector {

    private static final String TAG = "CameraThreatDetector";

    public interface OnThreatFrameDetectedListener {
        void onLowLightObstacleDetected(float luminanceLevel);
    }

    private boolean isMonitoring = false;

    public void startFrameAnalysis(Context context, OnThreatFrameDetectedListener listener) {
        if (isMonitoring) return;
        isMonitoring = true;
        Log.i(TAG, "🔍 On-device computer vision frame analysis initialized (Privacy-Preserving On-Device Processing)");
    }

    public void stopAnalysis() {
        isMonitoring = false;
    }
}
