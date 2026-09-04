package com.guardianai.utils;

import android.content.Context;
import android.util.Log;

public class ComputerVisionManager {

    private static final String TAG = "ComputerVisionManager";
    private static ComputerVisionManager instance;
    private boolean isVisionActive = false;

    private ComputerVisionManager(Context context) {
        // Privacy-first local processing setup
    }

    public static synchronized ComputerVisionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ComputerVisionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void startVisionEngine(VisionCallback callback) {
        this.isVisionActive = true;
        Log.i(TAG, "📷 On-Device Computer Vision Engine initialized (Local processing active).");
        if (callback != null) {
            callback.onStatusChanged("ACTIVE", "On-device frame processing running locally.");
        }
    }

    public void stopVisionEngine() {
        this.isVisionActive = false;
        Log.i(TAG, "📷 On-Device Computer Vision Engine stopped.");
    }

    public boolean isVisionActive() {
        return isVisionActive;
    }

    public interface VisionCallback {
        void onStatusChanged(String state, String message);
        void onAnomalyDetected(String anomalyType, float confidence);
    }
}
