import uuid
from datetime import datetime, timedelta, timezone
from fastapi import APIRouter, Depends, status, HTTPException
from typing import Dict, Any, List

from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.feature_schemas import CheckInStartRequest, CheckInResponse

router = APIRouter()

# In-memory session store for active safety check-in timers
_checkin_store: Dict[str, Dict[str, Any]] = {}

@router.post(
    "/start",
    response_model=CheckInResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Start safety check-in timer",
    description="Initiates a scheduled safety check-in timer. If user does not check in before expiration, guardian escalation is triggered."
)
def start_checkin(
    request: CheckInStartRequest,
    current_user: User = Depends(get_current_user)
):
    now = datetime.now(timezone.utc)
    expires = now + timedelta(minutes=request.duration_minutes)
    checkin_id = str(uuid.uuid4())
    
    checkin_data = {
        "id": checkin_id,
        "user_id": str(current_user.id),
        "status": "ACTIVE",
        "duration_minutes": request.duration_minutes,
        "started_at": now.isoformat(),
        "expires_at": expires.isoformat(),
        "destination": request.destination,
        "note": request.note
    }
    _checkin_store[str(current_user.id)] = checkin_data
    return CheckInResponse(**checkin_data)

@router.get(
    "/status",
    response_model=CheckInResponse,
    status_code=status.HTTP_200_OK,
    summary="Get active check-in timer status",
    description="Retrieves current active check-in timer state for authenticated user."
)
def get_checkin_status(
    current_user: User = Depends(get_current_user)
):
    user_id = str(current_user.id)
    if user_id not in _checkin_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No active check-in timer found."
        )
    
    data = _checkin_store[user_id]
    # Check expiry
    expires_dt = datetime.fromisoformat(data["expires_at"])
    if datetime.now(timezone.utc) > expires_dt and data["status"] == "ACTIVE":
        data["status"] = "ESCALATED"
        
    return CheckInResponse(**data)

@router.post(
    "/safe",
    response_model=CheckInResponse,
    status_code=status.HTTP_200_OK,
    summary="Check in as Safe ('I'm Safe')",
    description="Resolves and cancels active safety timer upon safe user check-in."
)
def checkin_safe(
    current_user: User = Depends(get_current_user)
):
    user_id = str(current_user.id)
    if user_id not in _checkin_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No active check-in timer to complete."
        )
    data = _checkin_store[user_id]
    data["status"] = "SAFE"
    return CheckInResponse(**data)
