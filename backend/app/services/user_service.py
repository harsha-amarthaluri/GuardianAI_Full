from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.db.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.user import UserResponse, UserUpdateRequest

class UserService:
    def __init__(self, db: Session):
        self.user_repo = UserRepository(db)

    def get_profile(self, user: User) -> UserResponse:
        return UserResponse.model_validate(user)

    def update_profile(self, user: User, update_req: UserUpdateRequest) -> UserResponse:
        if update_req.full_name is not None:
            user.full_name = update_req.full_name
        if update_req.phone_number is not None:
            user.phone_number = update_req.phone_number

        updated_user = self.user_repo.update(user)
        return UserResponse.model_validate(updated_user)

    def delete_account(self, user: User) -> None:
        self.user_repo.delete(user)
