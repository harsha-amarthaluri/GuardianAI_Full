package com.guardianai.data.sync;

import android.content.Context;
import android.util.Log;

import com.guardianai.auth.TokenManager;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.api.GuardianApiService;
import com.guardianai.data.local.AppDatabase;
import com.guardianai.data.local.LocationDao;
import com.guardianai.data.local.LocationEntity;
import com.guardianai.data.models.LocationBatchCreateRequest;
import com.guardianai.data.models.LocationBatchResponse;
import com.guardianai.data.models.LocationRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 25;

    private static SyncManager instance;

    private final Context context;
    private final LocationDao locationDao;
    private final GuardianApiService apiService;
    private final ExecutorService executor;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.locationDao = AppDatabase.getInstance(this.context).locationDao();
        this.apiService = ApiClient.getApiService(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    public void triggerSync() {
        if (isSyncing.compareAndSet(false, true)) {
            executor.execute(this::performSync);
        }
    }

    private void performSync() {
        try {
            // Check network state
            if (!NetworkMonitor.getInstance(context).isOnline()) {
                Log.d(TAG, "Network is offline. Sync postponed.");
                isSyncing.set(false);
                return;
            }

            // Check authentication state
            if (!TokenManager.getInstance(context).hasToken()) {
                Log.d(TAG, "User not authenticated. Sync postponed.");
                isSyncing.set(false);
                return;
            }

            List<LocationEntity> pending = locationDao.getPendingLocations(BATCH_SIZE);
            if (pending == null || pending.isEmpty()) {
                Log.d(TAG, "No pending locations to sync.");
                isSyncing.set(false);
                return;
            }

            Log.i(TAG, "Starting batch upload sync for " + pending.size() + " pending location points...");

            // Mark batch as UPLOADING
            List<Long> ids = new ArrayList<>();
            List<LocationRequestDto> dts = new ArrayList<>();
            for (LocationEntity entity : pending) {
                ids.add(entity.getId());
                dts.add(new LocationRequestDto(entity.getLatitude(), entity.getLongitude(), entity.getAccuracy()));
            }
            locationDao.updateStatus(ids, LocationEntity.STATUS_UPLOADING);

            LocationBatchCreateRequest batchRequest = new LocationBatchCreateRequest(dts);
            Response<LocationBatchResponse> response = apiService.recordLocationBatchSync(batchRequest).execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.i(TAG, "Batch upload successful. Processed: " + response.body().getProcessedCount() + ", Duplicates ignored: " + response.body().getIgnoredDuplicatesCount());
                locationDao.updateStatus(ids, LocationEntity.STATUS_SYNCED);
                locationDao.pruneSynced();
            } else {
                Log.w(TAG, "Batch upload failed with HTTP code: " + response.code() + ". Incrementing retry count...");
                handleBatchFailure(pending);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during sync: " + e.getMessage());
        } finally {
            isSyncing.set(false);
        }
    }

    private void handleBatchFailure(List<LocationEntity> pending) {
        for (LocationEntity entity : pending) {
            locationDao.incrementRetryAndCheckDeferred(entity.getId(), MAX_RETRIES);
        }
    }
}
