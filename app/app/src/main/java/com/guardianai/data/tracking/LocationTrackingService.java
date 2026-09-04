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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting LocationTrackingService foreground acquisition...");
        createNotificationChannel();
        Notification notification = buildNotification();

        // Start Foreground Service with ongoing notification
        startForeground(NOTIFICATION_ID, notification);

        TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.STARTING);
        startLocationUpdates();

        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                .setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.RUNNING);
            Log.i(TAG, "FusedLocationProviderClient requested updates successfully.");
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
