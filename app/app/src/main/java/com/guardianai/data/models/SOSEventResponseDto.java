package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SOSEventResponseDto {
    @SerializedName("id")
    private String id;

    @SerializedName("sos_id")
    private String sosId;

    @SerializedName("event_type")
    private String eventType;

    @SerializedName("actor_type")
    private String actorType;

    @SerializedName("status")
    private String status;

    @SerializedName("details")
    private Map<String, Object> details;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getSosId() { return sosId; }
    public String getEventType() { return eventType; }
    public String getActorType() { return actorType; }
    public String getStatus() { return status; }
    public Map<String, Object> getDetails() { return details; }
    public String getCreatedAt() { return createdAt; }
}
