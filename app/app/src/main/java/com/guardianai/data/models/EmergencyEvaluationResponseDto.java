package com.guardianai.data.models;

import com.google.gson.annotations.SerializedName;

public class EmergencyEvaluationResponseDto {
    @SerializedName("incident_level")
    private String incidentLevel;

    @SerializedName("action_recommended")
    private String actionRecommended;

    @SerializedName("confidence_score")
    private float confidenceScore;

    @SerializedName("auto_trigger_sos")
    private boolean autoTriggerSos;

    @SerializedName("requires_countdown_cancellation")
    private boolean requiresCountdownCancellation;

    @SerializedName("countdown_seconds")
    private int countdownSeconds;

    @SerializedName("reason")
    private String reason;

    public String getIncidentLevel() { return incidentLevel; }
    public String getActionRecommended() { return actionRecommended; }
    public float getConfidenceScore() { return confidenceScore; }
    public boolean isAutoTriggerSos() { return autoTriggerSos; }
    public boolean isRequiresCountdownCancellation() { return requiresCountdownCancellation; }
    public int getCountdownSeconds() { return countdownSeconds; }
    public String getReason() { return reason; }
}
