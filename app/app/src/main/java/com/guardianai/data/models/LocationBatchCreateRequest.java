package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LocationBatchCreateRequest {
    @SerializedName("locations")
    private List<LocationRequestDto> locations;

    public LocationBatchCreateRequest(List<LocationRequestDto> locations) {
        this.locations = locations;
    }

    public List<LocationRequestDto> getLocations() {
        return locations;
    }
}
