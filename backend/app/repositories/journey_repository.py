from typing import List, Optional, Tuple
from sqlalchemy.orm import Session
from app.db.models.journey import SafeJourney

class JourneyRepository:
    def __init__(self, db: Session):
        self.db = db

    def create(self, journey: SafeJourney) -> SafeJourney:
        self.db.add(journey)
        self.db.commit()
        self.db.refresh(journey)
        return journey

    def get_by_id_and_user_id(self, journey_id: str, user_id: str) -> Optional[SafeJourney]:
        return self.db.query(SafeJourney).filter(
            SafeJourney.id == journey_id,
            SafeJourney.user_id == user_id
        ).first()

    def get_active_user_journey(self, user_id: str) -> Optional[SafeJourney]:
        return self.db.query(SafeJourney).filter(
            SafeJourney.user_id == user_id,
            SafeJourney.status == "ACTIVE"
        ).order_by(SafeJourney.created_at.desc()).first()

    def list_user_journeys(self, user_id: str, skip: int = 0, limit: int = 20) -> Tuple[List[SafeJourney], int]:
        query = self.db.query(SafeJourney).filter(SafeJourney.user_id == user_id)
        total = query.count()
        items = query.order_by(SafeJourney.created_at.desc()).offset(skip).limit(limit).all()
        return items, total

    def update_status(self, journey_id: str, user_id: str, new_status: str) -> Optional[SafeJourney]:
        journey = self.get_by_id_and_user_id(journey_id, user_id)
        if journey:
            journey.status = new_status
            self.db.commit()
            self.db.refresh(journey)
        return journey
