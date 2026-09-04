from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.auth import UserRegisterRequest, UserLoginRequest, TokenResponse
from app.schemas.user import UserResponse
from app.services.auth_service import AuthService

router = APIRouter()

@router.post(
    "/register",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Register new user",
    description="Registers a new user account with validated email, phone, and hashed password."
)
def register(request: UserRegisterRequest, db: Session = Depends(get_db)):
    auth_service = AuthService(db)
    return auth_service.register_user(request)

@router.post(
    "/login",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="User login",
    description="Authenticates user credentials and returns a JWT access token."
)
def login(request: UserLoginRequest, db: Session = Depends(get_db)):
    auth_service = AuthService(db)
    return auth_service.login_user(request)

@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get authenticated user profile",
    description="Retrieves profile of the currently authenticated user."
)
def get_current_user_profile(current_user: User = Depends(get_current_user)):
    return UserResponse.model_validate(current_user)
