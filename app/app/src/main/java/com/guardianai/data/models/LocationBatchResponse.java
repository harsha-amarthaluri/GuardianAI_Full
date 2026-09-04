package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LocationBatchResponse {
    @SerializedName("processed_count")
    private int processedCount;

    @SerializedName("ignored_duplicates_count")
    private int ignoredDuplicatesCount;

    @SerializedName("items")
    private List<LocationResponseDto> items;

    public int getProcessedCount() { return processedCount; }
    public int getIgnoredDuplicatesCount() { return ignoredDuplicatesCount; }
    public List<LocationResponseDto> getItems() { return items; }
}
