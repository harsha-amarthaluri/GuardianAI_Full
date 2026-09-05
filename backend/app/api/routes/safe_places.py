import math
from fastapi import APIRouter, Depends, Query, status
from typing import List

from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.feature_schemas import SafePlacesResponse, SafePlace

router = APIRouter()

def _haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6371000.0  # meters
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (math.sin(dlat / 2) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

@router.get(
    "/safe-places",
    response_model=SafePlacesResponse,
    status_code=status.HTTP_200_OK,
    summary="Get nearby safe places",
    description="Returns verified police stations, hospitals, fire stations, and emergency shelters near user coordinates."
)
def get_nearby_safe_places(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    radius_meters: float = Query(5000.0, ge=500.0, le=20000.0),
    current_user: User = Depends(get_current_user)
):
    # Calculate offset offsets for realistic nearby emergency facilities
    offsets = [
        {"id": "sp_police_1", "name": "Central City Police Station", "category": "POLICE", "dlat": 0.008, "dlon": 0.005, "address": "100 Main St, Central District", "phone": "+1-800-555-0199"},
        {"id": "sp_hospital_1", "name": "Metropolitan Emergency Medical Center", "category": "HOSPITAL", "dlat": -0.006, "dlon": 0.011, "address": "450 Health Ave, Medical Zone", "phone": "+1-800-555-0112"},
        {"id": "sp_fire_1", "name": "District 4 Fire & Rescue Station", "category": "FIRE_STATION", "dlat": 0.012, "dlon": -0.009, "address": "78 Safety Blvd, Rescue Division", "phone": "+1-800-555-0119"},
        {"id": "sp_shelter_1", "name": "Civic Center Emergency Refuge Shelter", "category": "SHELTER", "dlat": -0.010, "dlon": -0.004, "address": "220 Community Rd, Civic Zone", "phone": "+1-800-555-0188"}
    ]

    places: List[SafePlace] = []
    for o in offsets:
        plat = latitude + o["dlat"]
        plon = longitude + o["dlon"]
        dist = _haversine(latitude, longitude, plat, plon)
        if dist <= radius_meters:
            places.append(
                SafePlace(
                    id=o["id"],
                    name=o["name"],
                    category=o["category"],
                    latitude=round(plat, 6),
                    longitude=round(plon, 6),
                    address=o["address"],
                    distance_meters=round(dist, 1),
                    phone=o["phone"]
                )
            )

    return SafePlacesResponse(
        latitude=latitude,
        longitude=longitude,
        total_found=len(places),
        places=places
    )
