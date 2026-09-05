package com.guardianai.data.tracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.guardianai.R;
import com.guardianai.ui.MainActivity;

public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    private static final String CHANNEL_ID = "guardian_tracking_channel";
    private static final int NOTIFICATION_ID = 2001;

    // Configurable Location Request Parameters
    private static final long UPDATE_INTERVAL_MS = 30000;    // 30 seconds
    private static final long FASTEST_INTERVAL_MS = 15000;  // 15 seconds
    private static final float MIN_DISPLACEMENT_METERS = 20.0f; // 20 meters

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationProcessor locationProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Creating LocationTrackingService...");
        locationProcessor = new LocationProcessor(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (android.location.Location location : locationResult.getLocations()) {
                    // Forward acquired location directly to LocationProcessor component
                    locationProcessor.processLocation(location);
                }
            }
        };
    }

    public static final String ACTION_SET_MODE = "com.guardianai.action.SET_TRACKING_MODE";
    public static final String EXTRA_MODE = "extra_tracking_mode";

    public enum TrackingMode {
        NORMAL,
        TRAVEL,
        EMERGENCY
    }

    public static void setMode(Context context, TrackingMode mode) {
        if (context == null || mode == null) return;
        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction(ACTION_SET_MODE);
        intent.putExtra(EXTRA_MODE, mode.name());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private TrackingMode currentMode = TrackingMode.NORMAL;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting LocationTrackingService foreground acquisition...");
        createNotificationChannel();
        Notification notification = buildNotification();

        // Start Foreground Service with ongoing notification
        startForeground(NOTIFICATION_ID, notification);

        if (intent != null && ACTION_SET_MODE.equals(intent.getAction())) {
            String modeStr = intent.getStringExtra(EXTRA_MODE);
            if (modeStr != null) {
                try {
                    currentMode = TrackingMode.valueOf(modeStr);
                    Log.i(TAG, "Switched tracking mode to: " + currentMode);
                } catch (Exception ignored) {}
            }
        }

        TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.STARTING);
        startLocationUpdates();

        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        long updateInterval = 30000;
        long fastestInterval = 15000;
        float minDisplacement = 20.0f;
        int priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        if (currentMode == TrackingMode.TRAVEL) {
            updateInterval = 10000;
            fastestInterval = 5000;
            minDisplacement = 5.0f;
            priority = Priority.PRIORITY_HIGH_ACCURACY;
        } else if (currentMode == TrackingMode.EMERGENCY) {
            updateInterval = 3000;
            fastestInterval = 1000;
            minDisplacement = 0.0f;
            priority = Priority.PRIORITY_HIGH_ACCURACY;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(priority, updateInterval)
                .setMinUpdateIntervalMillis(fastestInterval)
                .setMinUpdateDistanceMeters(minDisplacement)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            TrackingStateManager.getInstance().setState(
                    currentMode == TrackingMode.EMERGENCY ? TrackingStateManager.TrackingState.RUNNING : TrackingStateManager.TrackingState.RUNNING
            );
            Log.i(TAG, "FusedLocationProviderClient requested updates successfully (" + currentMode + " mode).");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException starting location updates: " + e.getMessage());
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.PERMISSION_REQUIRED);
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.ERROR);
            stopSelf();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.STOPPED);
        Log.i(TAG, "Location updates stopped.");
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        stopForeground(true);
        super.onDestroy();
        Log.i(TAG, "LocationTrackingService destroyed.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Guardian AI Safety Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Displays ongoing notification while background safety tracking is active.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Guardian AI Protection Active")
                .setContentText("Proactive threat awareness & safety tracking active.")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
