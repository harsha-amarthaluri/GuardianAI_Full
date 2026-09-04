from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field

class GuardianCreateRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100, json_schema_extra={"example": "John Doe"})
    phone: str = Field(..., min_length=5, max_length=30, json_schema_extra={"example": "+1987654321"})
    email: Optional[EmailStr] = Field(None, json_schema_extra={"example": "john.guardian@example.com"})
    relationship: str = Field(..., min_length=2, max_length=50, json_schema_extra={"example": "Parent"})
    notification_enabled: bool = True

class GuardianUpdateRequest(BaseModel):
    name: Optional[str] = Field(None, min_length=2, max_length=100)
    phone: Optional[str] = Field(None, min_length=5, max_length=30)
    email: Optional[EmailStr] = None
    relationship: Optional[str] = Field(None, min_length=2, max_length=50)
    notification_enabled: Optional[bool] = None

class GuardianResponse(BaseModel):
    id: str
    user_id: str
    name: str
    phone: str
    email: Optional[str] = None
    relationship: str
    notification_enabled: bool
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)
