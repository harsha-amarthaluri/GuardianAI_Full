from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.sos import SOSCreateRequest, SOSResponse, SOSListResponse
from app.services.sos_service import SOSService

router = APIRouter()

@router.post(
    "",
    response_model=SOSResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Trigger emergency SOS incident",
    description="Creates an active SOS emergency incident for the authenticated user and triggers notification stub."
)
def create_sos_incident(
    request: SOSCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = SOSService(db)
    return service.create_sos(current_user.id, request)

@router.get(
    "",
    response_model=SOSListResponse,
    status_code=status.HTTP_200_OK,
    summary="List user SOS incidents history",
    description="Returns paginated emergency incident history belonging strictly to the authenticated user."
)
def list_sos_incidents(
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = SOSService(db)
    return service.list_user_incidents(current_user.id, skip=skip, limit=limit)
