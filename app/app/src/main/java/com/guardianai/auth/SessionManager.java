package com.guardianai.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static SessionListener sessionListener;

    public interface SessionListener {
        void onSessionExpired();
    }

    public static void setSessionListener(SessionListener listener) {
        sessionListener = listener;
    }

    public static void handleSessionExpired(Context context) {
        Log.w(TAG, "HTTP 401 Unauthorized detected. Clearing session and triggering navigation...");
        if (context != null) {
            TokenManager.getInstance(context).clearToken();
        }
        if (sessionListener != null) {
            sessionListener.onSessionExpired();
        }
    }
}
