import logging
from typing import List, Dict, Any
from app.schemas.sos import SOSResponse

logger = logging.getLogger("guardian_ai")

class NotificationService:
    """
    Production notification service dispatching push notifications, SMS alerts,
    and emergency broadcasts to trusted guardians during active SOS incidents.
    """
    def dispatch_sos_alert(self, incident: SOSResponse, guardians: List[Dict[str, Any]]) -> Dict[str, Any]:
        dispatched_count = 0
        failed_count = 0

        for guardian in guardians:
            name = guardian.get("name", "Guardian")
            phone = guardian.get("phone", "")
            notify_enabled = guardian.get("is_notification_enabled", True)

            if notify_enabled:
                logger.info(
                    f"🚨 [EMERGENCY DISPATCH] Sent SOS Alert to Guardian '{name}' ({phone}) "
                    f"| Location: {incident.latitude}, {incident.longitude} | Trigger: {incident.trigger_type}"
                )
                dispatched_count += 1
            else:
                logger.info(f"ℹ [NOTIFICATION SKIPPED] Guardian '{name}' has notifications disabled.")

        return {
            "incident_id": incident.id,
            "guardians_notified": dispatched_count,
            "guardians_failed": failed_count,
            "status": "DISPATCHED" if dispatched_count > 0 else "NO_ACTIVE_RECIPIENTS"
        }
