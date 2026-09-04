import math
from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy.orm import Session

from app.repositories.threat_repository import ThreatRepository
from app.schemas.weather import RouteAnalysisRequest, RouteAnalysisResponse, CorridorOption

class RouteAnalysisService:
    """
    RouteAnalysisService evaluates route corridors against spatial threat density,
    calculating threat counts, cumulative severity, distance, and explainable risk levels.
    """
    def __init__(self, db: Session):
        self.repo = ThreatRepository(db)

    def analyze_route(self, req: RouteAnalysisRequest) -> RouteAnalysisResponse:
        dest_name = req.destination_name if req.destination_name else "Destination"

        # Calculate straight-line distance
        dist_km = self.repo.haversine_distance(
            req.start_latitude, req.start_longitude,
            req.end_latitude, req.end_longitude
        ) / 1000.0

        # Midpoint coordinate
        mid_lat = (req.start_latitude + req.end_latitude) / 2.0
        mid_lon = (req.start_longitude + req.end_longitude) / 2.0

        # Retrieve all spatial threats within a 10km radius of the corridor midpoint
        threats = self.repo.get_active_threats(mid_lat, mid_lon, radius_meters=10000.0)
        total_threats_count = len(threats)

        # Corridor A: Primary Direct Corridor
        corridor_a_threats = []
        corridor_a_severity = 0.0
        for t in threats:
            d_start = self.repo.haversine_distance(req.start_latitude, req.start_longitude, t.latitude, t.longitude)
            d_end = self.repo.haversine_distance(req.end_latitude, req.end_longitude, t.latitude, t.longitude)
            if d_start <= 3000.0 or d_end <= 3000.0:
                corridor_a_threats.append(t)
                corridor_a_severity += t.severity

        # Corridor B: Alternative Bypass Corridor
        corridor_b_threats = [t for t in threats if t not in corridor_a_threats]
        corridor_b_severity = sum(t.severity for t in corridor_b_threats)

        # Classify Corridor A
        if len(corridor_a_threats) == 0:
            level_a = "LOW"
            desc_a = "No active threats detected along primary corridor."
        elif len(corridor_a_threats) <= 2:
            level_a = "MODERATE"
            desc_a = f"{len(corridor_a_threats)} nearby threat alert(s) detected near corridor."
        else:
            level_a = "HIGH"
            desc_a = f"High threat density: {len(corridor_a_threats)} active alerts along primary corridor."

        # Classify Corridor B
        if len(corridor_b_threats) == 0:
            level_b = "LOW"
            desc_b = "Clear bypass corridor."
        else:
            level_b = "MODERATE" if len(corridor_b_threats) <= 3 else "HIGH"
            desc_b = f"Bypass route encounters {len(corridor_b_threats)} nearby threat alert(s)."

        corridors = [
            CorridorOption(
                corridor_name="Corridor A (Primary Route)",
                distance_km=round(max(0.5, dist_km), 2),
                nearby_threat_count=len(corridor_a_threats),
                threat_severity_sum=round(corridor_a_severity, 1),
                risk_level=level_a,
                description=desc_a
            ),
            CorridorOption(
                corridor_name="Corridor B (Bypass Route)",
                distance_km=round(max(0.6, dist_km * 1.15), 2),
                nearby_threat_count=len(corridor_b_threats),
                threat_severity_sum=round(corridor_b_severity, 1),
                risk_level=level_b,
                description=desc_b
            )
        ]

        # Recommended corridor selection
        rec = "Corridor A (Primary Route)" if corridor_a_severity <= corridor_b_severity else "Corridor B (Bypass Route)"

        return RouteAnalysisResponse(
            destination_name=dest_name,
            total_active_threats_in_region=total_threats_count,
            corridors=corridors,
            recommended_corridor=rec,
            timestamp=datetime.now(timezone.utc).isoformat()
        )
