from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class FCMTokenCreate(BaseModel):
    device_id: str
    fcm_token: str
    platform: Optional[str] = "android"

class FCMTokenResponse(BaseModel):
    id: str
    user_id: str
    device_id: str
    fcm_token: str
    platform: str
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class PushNotificationDispatch(BaseModel):
    title: str
    body: str
    incident_id: Optional[str] = None
    target_user_id: Optional[str] = None
