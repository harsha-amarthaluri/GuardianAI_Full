package com.guardianai.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_locations")
public class LocationEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_UPLOADING = "UPLOADING";
    public static final String STATUS_SYNCED = "SYNCED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_FAILED_RETRY_DEFERRED = "FAILED_RETRY_DEFERRED";

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "accuracy")
    private Float accuracy;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "sync_status")
    private String syncStatus;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "retry_count")
    private int retryCount;

    public LocationEntity(double latitude, double longitude, Float accuracy, String timestamp, String syncStatus, long createdAt, int retryCount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.syncStatus = syncStatus;
        this.createdAt = createdAt;
        this.retryCount = retryCount;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Float getAccuracy() { return accuracy; }
    public String getTimestamp() { return timestamp; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public long getCreatedAt() { return createdAt; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
