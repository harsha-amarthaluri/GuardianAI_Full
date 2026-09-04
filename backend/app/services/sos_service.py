from typing import List, Tuple
from sqlalchemy.orm import Session

from app.db.models.sos import SOSIncident
from app.repositories.sos_repository import SOSRepository
from app.repositories.guardian_repository import GuardianRepository
from app.schemas.sos import SOSCreateRequest, SOSResponse, SOSListResponse, IncidentStatusEnum
from app.services.notification_service import NotificationService

class SOSService:
    def __init__(self, db: Session):
        self.sos_repo = SOSRepository(db)
        self.guardian_repo = GuardianRepository(db)
        self.notification_service = NotificationService()

    def create_sos(self, user_id: str, request: SOSCreateRequest) -> SOSResponse:
        incident = SOSIncident(
            user_id=user_id,
            latitude=request.latitude,
            longitude=request.longitude,
            trigger_type=request.trigger_type.value,
            status=IncidentStatusEnum.ALERTING.value
        )
        created = self.sos_repo.create(incident)
        response = SOSResponse.model_validate(created)

        # Dispatch real guardian notifications via NotificationService
        guardians = self.guardian_repo.get_by_user_id(user_id)
        guardian_dicts = [
            {
                "name": g.name,
                "phone": g.phone,
                "email": g.email,
                "is_notification_enabled": g.notification_enabled
            }
            for g in guardians
        ]
        self.notification_service.dispatch_sos_alert(response, guardian_dicts)

        return response

    def list_user_incidents(self, user_id: str, skip: int = 0, limit: int = 20) -> SOSListResponse:
        items, total = self.sos_repo.get_by_user_id(user_id, skip=skip, limit=limit)
        response_items = [SOSResponse.model_validate(inc) for inc in items]
        return SOSListResponse(
            items=response_items,
            total=total,
            skip=skip,
            limit=limit
        )
