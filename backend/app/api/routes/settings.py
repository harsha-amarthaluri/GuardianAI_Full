from datetime import datetime, timezone
from fastapi import APIRouter, Depends, status
from typing import Dict, Any

from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.feature_schemas import UserSettingsUpdate, UserSettingsResponse

router = APIRouter()

# In-memory settings store for user preference configurations
_user_settings: Dict[str, Dict[str, Any]] = {}

@router.get(
    "",
    response_model=UserSettingsResponse,
    status_code=status.HTTP_200_OK,
    summary="Get user security and privacy settings",
    description="Retrieves active user security, privacy, voice distress, motion detection, and SOS preferences."
)
def get_settings(
    current_user: User = Depends(get_current_user)
):
    user_id = str(current_user.id)
    if user_id not in _user_settings:
        _user_settings[user_id] = {
            "user_id": user_id,
            "shake_sos_enabled": True,
            "fall_detection_enabled": True,
            "voice_distress_enabled": True,
            "motion_detection_enabled": True,
            "evidence_recording_enabled": True,
            "location_sharing_enabled": True,
            "dark_mode_enabled": True,
            "emergency_contacts_only": False,
            "updated_at": datetime.now(timezone.utc).isoformat()
        }
    return UserSettingsResponse(**_user_settings[user_id])

@router.put(
    "",
    response_model=UserSettingsResponse,
    status_code=status.HTTP_200_OK,
    summary="Update user security and privacy settings",
    description="Updates user configuration preferences for automated detection, privacy controls, and SOS features."
)
def update_settings(
    update_data: UserSettingsUpdate,
    current_user: User = Depends(get_current_user)
):
    user_id = str(current_user.id)
    current = get_settings(current_user).dict()
    
    update_dict = update_data.dict(exclude_unset=True)
    for k, v in update_dict.items():
        if v is not None:
            current[k] = v
            
    current["updated_at"] = datetime.now(timezone.utc).isoformat()
    _user_settings[user_id] = current
    return UserSettingsResponse(**current)
