from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies import get_current_user
from app.schemas.journey import JourneyStartRequest, JourneyPingRequest, JourneyResponse, JourneyListResponse
from app.services.journey_service import JourneyService

router = APIRouter(prefix="/journeys", tags=["Safe Journeys"])

@router.post("/start", response_model=JourneyResponse, status_code=status.HTTP_201_CREATED)
def start_journey(
    request: JourneyStartRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = JourneyService(db)
    return service.start_journey(current_user.id, request)

@router.get("/active", response_model=Optional[JourneyResponse])
def get_active_journey(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = JourneyService(db)
    return service.get_active_journey(current_user.id)

@router.post("/{journey_id}/complete", response_model=JourneyResponse)
def complete_journey(
    journey_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = JourneyService(db)
    completed = service.complete_journey(current_user.id, journey_id)
    if not completed:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Active journey not found"
        )
    return completed

@router.get("", response_model=JourneyListResponse)
def list_journeys(
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = JourneyService(db)
    return service.list_journeys(current_user.id, skip=skip, limit=limit)
