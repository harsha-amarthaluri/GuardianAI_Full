from typing import Optional
from sqlalchemy.orm import Session

from app.db.models.journey import SafeJourney
from app.repositories.journey_repository import JourneyRepository
from app.schemas.journey import JourneyStartRequest, JourneyPingRequest, JourneyResponse, JourneyListResponse

class JourneyService:
    def __init__(self, db: Session):
        self.db = db
        self.repo = JourneyRepository(db)

    def start_journey(self, user_id: str, request: JourneyStartRequest) -> JourneyResponse:
        # Cancel any previous active journey
        existing = self.repo.get_active_user_journey(user_id)
        if existing:
            self.repo.update_status(existing.id, user_id, "CANCELLED")

        journey = SafeJourney(
            user_id=user_id,
            origin_latitude=request.origin_latitude,
            origin_longitude=request.origin_longitude,
            destination_address=request.destination_address,
            destination_latitude=request.destination_latitude,
            destination_longitude=request.destination_longitude,
            expected_arrival_time=request.expected_arrival_time,
            status="ACTIVE"
        )
        created = self.repo.create(journey)
        return JourneyResponse.model_validate(created)

    def get_active_journey(self, user_id: str) -> Optional[JourneyResponse]:
        journey = self.repo.get_active_user_journey(user_id)
        if not journey:
            return None
        return JourneyResponse.model_validate(journey)

    def complete_journey(self, user_id: str, journey_id: str) -> Optional[JourneyResponse]:
        updated = self.repo.update_status(journey_id, user_id, "COMPLETED")
        if not updated:
            return None
        return JourneyResponse.model_validate(updated)

    def list_journeys(self, user_id: str, skip: int = 0, limit: int = 20) -> JourneyListResponse:
        items, total = self.repo.list_user_journeys(user_id, skip=skip, limit=limit)
        return JourneyListResponse(
            items=[JourneyResponse.model_validate(j) for j in items],
            total=total,
            skip=skip,
            limit=limit
        )
