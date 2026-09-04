from typing import List, Optional
from sqlalchemy.orm import Session
from app.repositories.threat_repository import ThreatRepository
from app.schemas.threat import ThreatCreate, ThreatResponse, ThreatListResponse

class ThreatService:
    def __init__(self, db: Session):
        self.repo = ThreatRepository(db)

    def create_threat(self, threat_data: ThreatCreate) -> ThreatResponse:
        threat = self.repo.create(threat_data)
        return ThreatResponse.model_validate(threat)

    def get_nearby_threats(
        self,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        radius_meters: float = 5000.0,
        category: Optional[str] = None
    ) -> ThreatListResponse:
        threats = self.repo.get_active_threats(latitude, longitude, radius_meters, category)
        items = [ThreatResponse.model_validate(t) for t in threats]
        return ThreatListResponse(items=items, total=len(items))
