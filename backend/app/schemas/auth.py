from typing import Optional
from pydantic import BaseModel, EmailStr, Field

class UserRegisterRequest(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=100, json_schema_extra={"example": "Jane Doe"})
    email: EmailStr = Field(..., json_schema_extra={"example": "jane.doe@example.com"})
    phone_number: Optional[str] = Field(None, max_length=30, json_schema_extra={"example": "+1234567890"})
    password: str = Field(..., min_length=8, max_length=100, json_schema_extra={"example": "SecurePassword123!"})

class UserLoginRequest(BaseModel):
    email: EmailStr = Field(..., json_schema_extra={"example": "jane.doe@example.com"})
    password: str = Field(..., json_schema_extra={"example": "SecurePassword123!"})

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int = 86400
