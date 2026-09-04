package com.guardianai;

import com.guardianai.data.local.LocationEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocationQueueStateTest {

    @Test
    public void testLocationEntityStatusConstants() {
        assertEquals("PENDING", LocationEntity.STATUS_PENDING);
        assertEquals("UPLOADING", LocationEntity.STATUS_UPLOADING);
        assertEquals("SYNCED", LocationEntity.STATUS_SYNCED);
        assertEquals("FAILED", LocationEntity.STATUS_FAILED);
        assertEquals("FAILED_RETRY_DEFERRED", LocationEntity.STATUS_FAILED_RETRY_DEFERRED);
    }

    @Test
    public void testLocationEntityInstantiation() {
        LocationEntity entity = new LocationEntity(
                37.7749, -122.4194, 5.0f,
                "2026-08-25T10:00:00Z", LocationEntity.STATUS_PENDING,
                System.currentTimeMillis(), 0
        );

        assertEquals(37.7749, entity.getLatitude(), 0.0001);
        assertEquals(-122.4194, entity.getLongitude(), 0.0001);
        assertEquals(5.0f, entity.getAccuracy(), 0.01f);
        assertEquals("PENDING", entity.getSyncStatus());
        assertEquals(0, entity.getRetryCount());

        entity.setSyncStatus(LocationEntity.STATUS_FAILED_RETRY_DEFERRED);
        assertEquals("FAILED_RETRY_DEFERRED", entity.getSyncStatus());

        entity.setRetryCount(5);
        assertEquals(5, entity.getRetryCount());
    }
}
