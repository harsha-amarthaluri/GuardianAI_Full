import logging
from fastapi import APIRouter, Depends
from app.schemas.emergency import EmergencyEvaluationRequest, EmergencyEvaluationResponse
from app.db.models.user import User
from app.dependencies import get_current_user

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/emergency", tags=["Emergency Response Engine"])

@router.post("/evaluate", response_model=EmergencyEvaluationResponse)
def evaluate_emergency_incident(
    req: EmergencyEvaluationRequest,
    current_user: User = Depends(get_current_user)
):
    trigger = req.trigger_type.upper()
    accuracy = req.gps_accuracy or 10.0
    battery = req.battery_level or 100
    density = req.threat_density or 0.0

    severity = "NORMAL"
    confidence = 0.5
    action = "Continue monitoring situational surroundings."
    requires_alert = False

    if trigger == "MANUAL" or trigger == "SOS":
        severity = "CRITICAL"
        confidence = 0.99
        action = "Immediate SOS alert dispatch. Alert trusted guardians and log GPS coordinates."
        requires_alert = True
    elif trigger == "SHAKE" or trigger == "FALL":
        severity = "HIGH"
        confidence = 0.85
        action = "Motion sensor distress detected. Prompt 3-second user countdown before guardian escalation."
        requires_alert = True
    elif trigger == "STILLNESS":
        severity = "WARNING"
        confidence = 0.70
        action = "Unusual stillness detected along route corridor. Check user safety status."
        requires_alert = False
    elif trigger == "VOICE":
        severity = "HIGH"
        confidence = 0.90
        action = "Voice distress keyword detected. Initiating emergency response protocol."
        requires_alert = True
    else:
        if density > 50.0:
            severity = "WARNING"
            confidence = 0.60
            action = "Entering high threat density zone. Recommend alternate safe corridor."

    if battery < 15 and not req.is_charging:
        action += " Low battery alert active (<15%)."

    logger.info(f"Evaluated emergency incident for user {current_user.id}: Trigger={trigger}, Severity={severity}")

    return EmergencyEvaluationResponse(
        severity=severity,
        confidence=confidence,
        recommended_action=action,
        requires_guardian_alert=requires_alert
    )
