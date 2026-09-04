from typing import List, Optional, Tuple
from sqlalchemy.orm import Session
from app.db.models.sos import SOSIncident

class SOSRepository:
    def __init__(self, db: Session):
        self.db = db

    def create(self, incident: SOSIncident) -> SOSIncident:
        self.db.add(incident)
        self.db.commit()
        self.db.refresh(incident)
        return incident

    def get_by_user_id(self, user_id: str, skip: int = 0, limit: int = 20) -> Tuple[List[SOSIncident], int]:
        query = self.db.query(SOSIncident).filter(SOSIncident.user_id == user_id)
        total = query.count()
        items = query.order_by(SOSIncident.created_at.desc()).offset(skip).limit(limit).all()
        return items, total

    def get_by_id_and_user_id(self, incident_id: str, user_id: str) -> Optional[SOSIncident]:
        return self.db.query(SOSIncident).filter(
            SOSIncident.id == incident_id,
            SOSIncident.user_id == user_id
        ).first()
