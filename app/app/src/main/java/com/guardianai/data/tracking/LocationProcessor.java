package com.guardianai.data.tracking;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.guardianai.data.local.AppDatabase;
import com.guardianai.data.local.LocationDao;
import com.guardianai.data.local.LocationEntity;
import com.guardianai.data.sync.SyncManager;
import com.guardianai.utils.ValidationUtils;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationProcessor {

    private static final String TAG = "LocationProcessor";

    private final LocationDao locationDao;
    private final SyncManager syncManager;
    private final ExecutorService executor;

    private Location lastProcessedLocation;
    private long lastProcessedTimeMs;

    public LocationProcessor(Context context) {
        this.locationDao = AppDatabase.getInstance(context).locationDao();
        this.syncManager = SyncManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void processLocation(Location location) {
        if (location == null) return;

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        float accuracy = location.hasAccuracy() ? location.getAccuracy() : 0.0f;
        long timeMs = location.getTime();

        // 1. Coordinate Validation
        if (!ValidationUtils.isValidLatitude(lat) || !ValidationUtils.isValidLongitude(lon)) {
            Log.w(TAG, "Rejected invalid GPS coordinates.");
            return;
        }

        // 2. Reject accuracy if unusable (> 100 meters)
        if (location.hasAccuracy() && accuracy > 100.0f) {
            Log.w(TAG, "Rejected low-accuracy GPS point: " + accuracy + "m");
            return;
        }

        // 3. Minimal Client-Side Duplicate Protection (distance < 1m AND time < 1s)
        if (lastProcessedLocation != null) {
            float distanceMeters = lastProcessedLocation.distanceTo(location);
            long timeDiffMs = Math.abs(timeMs - lastProcessedTimeMs);
            if (distanceMeters < 1.0f && timeDiffMs < 1000) {
                Log.d(TAG, "Minimal client-side duplicate filter suppressed identical GPS update.");
                return;
            }
        }

        lastProcessedLocation = location;
        lastProcessedTimeMs = timeMs;

        // ISO-8601 UTC timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String isoTimestamp = sdf.format(new Date(timeMs));

        // 4. Persistence into Room DB Queue
        executor.execute(() -> {
            try {
                // Prune oldest records if queue exceeds retention capacity (max 1000)
                int currentCount = locationDao.getRecordCount();
                if (currentCount >= 1000) {
                    locationDao.deleteOldestRecords(50);
                    Log.w(TAG, "Queue retention limit reached (1000 records). Pruned 50 oldest records.");
                }

                LocationEntity entity = new LocationEntity(
                        lat, lon, accuracy, isoTimestamp,
                        LocationEntity.STATUS_PENDING, System.currentTimeMillis(), 0
                );
                locationDao.insert(entity);
                Log.d(TAG, "Persisted location point to Room database queue.");

                // Trigger SyncManager
                syncManager.triggerSync();
            } catch (Exception e) {
                Log.e(TAG, "Error saving location to local database: " + e.getMessage());
            }
        });
    }
}
