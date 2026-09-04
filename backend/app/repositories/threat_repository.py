import math
from typing import List, Optional
from sqlalchemy.orm import Session
from app.db.models.threat import Threat
from app.schemas.threat import ThreatCreate

class ThreatRepository:
    def __init__(self, db: Session):
        this_db = db
        self.db = this_db

    def create(self, threat_data: ThreatCreate) -> Threat:
        threat = Threat(
            category=threat_data.category.upper(),
            severity=threat_data.severity,
            title=threat_data.title,
            description=threat_data.description,
            latitude=threat_data.latitude,
            longitude=threat_data.longitude,
            radius=threat_data.radius,
            source=threat_data.source,
            confidence=threat_data.confidence,
            is_active=threat_data.is_active
        )
        self.db.add(threat)
        self.db.commit()
        self.db.refresh(threat)
        return threat

    def get_active_threats(
        self,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        radius_meters: Optional[float] = 5000.0,
        category: Optional[str] = None
    ) -> List[Threat]:
        query = self.db.query(Threat).filter(Threat.is_active == True)

        if category and category.upper() != "ALL":
            query = query.filter(Threat.category == category.upper())

        all_active = query.all()

        if latitude is None or longitude is None:
            return all_active

        # Filter by haversine distance in Python to support all DB engines
        results = []
        for t in all_active:
            dist = self.haversine_distance(latitude, longitude, t.latitude, t.longitude)
            if dist <= (radius_meters or 5000.0):
                results.append(t)
        return results

    @staticmethod
    def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """Returns distance in meters between two lat/lon coordinates."""
        R = 6371000.0  # Earth radius in meters
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)

        a = math.sin(delta_phi / 2.0) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2.0) ** 2
        c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
        return R * c
