from typing import List
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.db.models.guardian import Guardian
from app.repositories.guardian_repository import GuardianRepository
from app.schemas.guardian import GuardianCreateRequest, GuardianUpdateRequest, GuardianResponse

class GuardianService:
    def __init__(self, db: Session):
        self.guardian_repo = GuardianRepository(db)

    def create_guardian(self, user_id: str, request: GuardianCreateRequest) -> GuardianResponse:
        guardian = Guardian(
            user_id=user_id,
            name=request.name,
            phone=request.phone,
            email=request.email,
            relationship=request.relationship,
            notification_enabled=request.notification_enabled
        )
        created = self.guardian_repo.create(guardian)
        return GuardianResponse.model_validate(created)

    def list_guardians(self, user_id: str) -> List[GuardianResponse]:
        guardians = self.guardian_repo.get_by_user_id(user_id)
        return [GuardianResponse.model_validate(g) for g in guardians]

    def get_guardian(self, guardian_id: str, user_id: str) -> GuardianResponse:
        guardian = self.guardian_repo.get_by_id_and_user_id(guardian_id, user_id)
        if not guardian:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Guardian not found."
            )
        return GuardianResponse.model_validate(guardian)

    def update_guardian(self, guardian_id: str, user_id: str, request: GuardianUpdateRequest) -> GuardianResponse:
        guardian = self.guardian_repo.get_by_id_and_user_id(guardian_id, user_id)
        if not guardian:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Guardian not found."
            )

        if request.name is not None:
            guardian.name = request.name
        if request.phone is not None:
            guardian.phone = request.phone
        if request.email is not None:
            guardian.email = request.email
        if request.relationship is not None:
            guardian.relationship = request.relationship
        if request.notification_enabled is not None:
            guardian.notification_enabled = request.notification_enabled

        updated = self.guardian_repo.update(guardian)
        return GuardianResponse.model_validate(updated)

    def delete_guardian(self, guardian_id: str, user_id: str) -> None:
        guardian = self.guardian_repo.get_by_id_and_user_id(guardian_id, user_id)
        if not guardian:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Guardian not found."
            )
        self.guardian_repo.delete(guardian)
