from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.threat import ThreatCreate, ThreatResponse, ThreatListResponse
from app.services.threat_service import ThreatService

router = APIRouter()

@router.get(
    "",
    response_model=ThreatListResponse,
    status_code=status.HTTP_200_OK,
    summary="Get active threats",
    description="Retrieves active threat events filtered by coordinates, radius, and category."
)
def get_threats(
    latitude: Optional[float] = Query(None, ge=-90.0, le=90.0),
    longitude: Optional[float] = Query(None, ge=-180.0, le=180.0),
    radius: float = Query(5000.0, ge=100.0, le=50000.0),
    category: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = ThreatService(db)
    return service.get_nearby_threats(latitude, longitude, radius, category)

@router.post(
    "",
    response_model=ThreatResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create active threat",
    description="Registers a new active threat event in the system database."
)
def create_threat(
    request: ThreatCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = ThreatService(db)
    return service.create_threat(request)
