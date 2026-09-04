from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.core.security import get_password_hash, verify_password, create_access_token
from app.db.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.auth import UserRegisterRequest, UserLoginRequest, TokenResponse
from app.schemas.user import UserResponse

class AuthService:
    def __init__(self, db: Session):
        self.user_repo = UserRepository(db)

    def register_user(self, request: UserRegisterRequest) -> UserResponse:
        # Check duplicate email
        existing_user = self.user_repo.get_by_email(request.email)
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email address already exists."
            )

        hashed_password = get_password_hash(request.password)
        new_user = User(
            full_name=request.full_name,
            email=request.email.lower(),
            phone_number=request.phone_number,
            hashed_password=hashed_password,
            role="USER",
            is_active=True,
            is_verified=False
        )

        created_user = self.user_repo.create(new_user)
        return UserResponse.model_validate(created_user)

    def login_user(self, request: UserLoginRequest) -> TokenResponse:
        user = self.user_repo.get_by_email(request.email)
        if not user or not verify_password(request.password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password.",
                headers={"WWW-Authenticate": "Bearer"}
            )

        if not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Account is inactive."
            )

        access_token = create_access_token(subject=user.id)
        return TokenResponse(
            access_token=access_token,
            token_type="bearer",
            expires_in=86400
        )
