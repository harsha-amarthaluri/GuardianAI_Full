import math
from datetime import datetime, timezone
from typing import List
from sqlalchemy.orm import Session
from app.db.models.location import Location
from app.repositories.location_repository import LocationRepository
from app.schemas.location import (
    LocationCreateRequest,
    LocationResponse,
    LocationBatchCreateRequest,
    LocationBatchResponse
)

def haversine_distance_meters(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Calculate Great Circle distance in meters between two lat/lon points."""
    R = 6371000.0  # Earth radius in meters
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi / 2.0)**2 + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2.0)**2
    c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
    return R * c

class LocationService:
    def __init__(self, db: Session):
        self.location_repo = LocationRepository(db)

    def record_location(self, user_id: str, request: LocationCreateRequest) -> LocationResponse:
        recorded_time = request.timestamp if request.timestamp else datetime.now(timezone.utc)
        location = Location(
            user_id=user_id,
            latitude=request.latitude,
            longitude=request.longitude,
            accuracy=request.accuracy,
            timestamp=recorded_time
        )
        created = self.location_repo.create(location)
        return LocationResponse.model_validate(created)

    def record_location_batch(self, user_id: str, request: LocationBatchCreateRequest) -> LocationBatchResponse:
        """
        Processes batch location submission with authoritative server-side duplicate suppression.
        Ignores points for the same user if recorded within 2 seconds and < 5 meters displacement of previous point.
        """
        recent_locations = self.location_repo.get_user_locations(user_id, limit=5)
        saved_items: List[LocationResponse] = []
        ignored_count = 0

        # Sort request points by timestamp if provided
        sorted_requests = sorted(
            request.locations,
            key=lambda loc: loc.timestamp if loc.timestamp else datetime.now(timezone.utc)
        )

        for loc_req in sorted_requests:
            recorded_time = loc_req.timestamp if loc_req.timestamp else datetime.now(timezone.utc)
            # Ensure recorded_time is timezone-naive for uniform comparison
            recorded_time_naive = recorded_time.replace(tzinfo=None) if recorded_time.tzinfo else recorded_time

            is_duplicate = False
            for prev in recent_locations:
                prev_time_naive = prev.timestamp.replace(tzinfo=None) if prev.timestamp.tzinfo else prev.timestamp
                # Calculate time difference
                time_diff = abs((recorded_time_naive - prev_time_naive).total_seconds())
                if time_diff <= 2.0:
                    dist = haversine_distance_meters(loc_req.latitude, loc_req.longitude, prev.latitude, prev.longitude)
                    if dist < 5.0:
                        is_duplicate = True
                        break

            if is_duplicate:
                ignored_count += 1
                continue


            location_model = Location(
                user_id=user_id,
                latitude=loc_req.latitude,
                longitude=loc_req.longitude,
                accuracy=loc_req.accuracy,
                timestamp=recorded_time
            )
            created = self.location_repo.create(location_model)
            response_dto = LocationResponse.model_validate(created)
            saved_items.append(response_dto)
            recent_locations.insert(0, created)

        return LocationBatchResponse(
            processed_count=len(saved_items),
            ignored_duplicates_count=ignored_count,
            items=saved_items
        )
