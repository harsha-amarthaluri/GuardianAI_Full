import logging
from typing import List
from app.schemas.sos import SOSResponse

logger = logging.getLogger("guardian_ai")

class NotificationServiceStub:
    """
    Stub interface for emergency notifications (SMS/Push/Email).
    This will be implemented in subsequent phases without altering the SOS service logic.
    """
    def notify_guardians_sos(self, incident: SOSResponse, guardian_phones: List[str]) -> bool:
        logger.info(f"[NOTIFICATION STUB] Triggering SOS notification for incident {incident.id} to guardians: {guardian_phones}")
        # Stub implementation returning success
        return True
