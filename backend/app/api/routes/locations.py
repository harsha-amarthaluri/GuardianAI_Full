from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.location import (
    LocationCreateRequest,
    LocationResponse,
    LocationBatchCreateRequest,
    LocationBatchResponse
)
from app.services.location_service import LocationService

router = APIRouter()

@router.post(
    "",
    response_model=LocationResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Record single location telemetry point",
    description="Submits client GPS location point for the authenticated user."
)
def record_location(
    request: LocationCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = LocationService(db)
    return service.record_location(current_user.id, request)

@router.post(
    "/batch",
    response_model=LocationBatchResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Record batch location telemetry points",
    description="Submits batch of queued offline GPS location points for the authenticated user with authoritative server-side deduplication."
)
def record_location_batch(
    request: LocationBatchCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = LocationService(db)
    return service.record_location_batch(current_user.id, request)
