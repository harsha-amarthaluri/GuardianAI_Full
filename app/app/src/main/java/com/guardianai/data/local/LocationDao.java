package com.guardianai.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface LocationDao {

    @Insert
    long insert(LocationEntity location);

    @Query("SELECT * FROM pending_locations WHERE sync_status = 'PENDING' OR sync_status = 'FAILED' ORDER BY id ASC LIMIT :limit")
    List<LocationEntity> getPendingLocations(int limit);

    @Query("UPDATE pending_locations SET sync_status = :status WHERE id IN (:ids)")
    void updateStatus(List<Long> ids, String status);

    @Query("UPDATE pending_locations SET retry_count = retry_count + 1, sync_status = CASE WHEN retry_count + 1 >= :maxRetries THEN 'FAILED_RETRY_DEFERRED' ELSE 'FAILED' END WHERE id = :id")
    void incrementRetryAndCheckDeferred(long id, int maxRetries);

    @Query("DELETE FROM pending_locations WHERE sync_status = 'SYNCED'")
    void pruneSynced();

    @Query("DELETE FROM pending_locations WHERE id IN (SELECT id FROM pending_locations ORDER BY id ASC LIMIT :count)")
    void deleteOldestRecords(int count);

    @Query("SELECT COUNT(*) FROM pending_locations")
    int getRecordCount();
}
