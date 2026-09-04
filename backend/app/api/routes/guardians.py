from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.guardian import GuardianCreateRequest, GuardianUpdateRequest, GuardianResponse
from app.services.guardian_service import GuardianService

router = APIRouter()

@router.post(
    "",
    response_model=GuardianResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create guardian",
    description="Adds a trusted contact as a guardian for the authenticated user."
)
def create_guardian(
    request: GuardianCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = GuardianService(db)
    return service.create_guardian(current_user.id, request)

@router.get(
    "",
    response_model=List[GuardianResponse],
    status_code=status.HTTP_200_OK,
    summary="List guardians",
    description="Returns all guardians created by the authenticated user."
)
def list_guardians(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = GuardianService(db)
    return service.list_guardians(current_user.id)

@router.get(
    "/{guardian_id}",
    response_model=GuardianResponse,
    status_code=status.HTTP_200_OK,
    summary="Get guardian by ID",
    description="Retrieves specific guardian details belonging to the authenticated user."
)
def get_guardian(
    guardian_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = GuardianService(db)
    return service.get_guardian(guardian_id, current_user.id)

@router.put(
    "/{guardian_id}",
    response_model=GuardianResponse,
    status_code=status.HTTP_200_OK,
    summary="Update guardian",
    description="Updates specific guardian details belonging to the authenticated user."
)
def update_guardian(
    guardian_id: str,
    request: GuardianUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = GuardianService(db)
    return service.update_guardian(guardian_id, current_user.id, request)

@router.delete(
    "/{guardian_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete guardian",
    description="Deletes a specific guardian belonging to the authenticated user."
)
def delete_guardian(
    guardian_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = GuardianService(db)
    service.delete_guardian(guardian_id, current_user.id)
    return None
