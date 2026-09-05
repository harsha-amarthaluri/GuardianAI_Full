package com.guardianai.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.guardianai.ui.MainActivity;

public class GuardianFirebaseMessagingService {

    private static final String TAG = "GuardianFCM";
    private static final String CHANNEL_ID = "guardian_emergency_alerts";

    public static void showEmergencyNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Guardian Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("High priority emergency notifications and threat alerts");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
        Log.i(TAG, "🚨 [FCM NOTIFICATION DISPATCHED] Title: " + title + " | Message: " + message);
    }

    public static void registerFcmTokenWithBackend(Context context, String token) {
        if (token == null || token.trim().isEmpty()) return;
        com.guardianai.data.api.ApiClient.getApiService(context)
                .registerFcmToken(new com.guardianai.data.models.FCMTokenRequestDto(token, "android"))
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Successfully registered FCM token with backend.");
                        } else {
                            Log.w(TAG, "Failed to register FCM token. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error registering FCM token with backend", t);
                    }
                });
    }
}
