from pydantic import BaseModel, Field
from typing import Optional

class EmergencyEvaluationRequest(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    trigger_type: str = Field(..., description="MANUAL, SHAKE, FALL, STILLNESS, VOICE")
    gps_accuracy: Optional[float] = 10.0
    battery_level: Optional[int] = 100
    is_charging: Optional[bool] = False
    threat_density: Optional[float] = 0.0

class EmergencyEvaluationResponse(BaseModel):
    severity: str  # NORMAL, WARNING, HIGH, CRITICAL
    confidence: float
    recommended_action: str
    requires_guardian_alert: bool
