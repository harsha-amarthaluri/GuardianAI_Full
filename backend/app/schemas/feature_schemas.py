from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class ChatMessageRequest(BaseModel):
    message: str = Field(..., min_length=1, description="User query or distress prompt")
    context: Optional[List[Dict[str, str]]] = Field(default=None, description="Recent message history")
    latitude: Optional[float] = Field(default=None, ge=-90.0, le=90.0)
    longitude: Optional[float] = Field(default=None, ge=-180.0, le=180.0)

class ChatMessageResponse(BaseModel):
    reply: str
    is_emergency_detected: bool = False
    suggested_actions: List[str] = []
    timestamp: str

class CheckInStartRequest(BaseModel):
    duration_minutes: int = Field(..., ge=1, le=1440, description="Timer duration in minutes")
    destination: Optional[str] = Field(default=None, description="Optional target destination")
    note: Optional[str] = Field(default=None, description="Optional note for guardians")

class CheckInResponse(BaseModel):
    id: str
    status: str  # ACTIVE, SAFE, EXPIRED, ESCALATED
    duration_minutes: int
    started_at: str
    expires_at: str
    destination: Optional[str] = None
    note: Optional[str] = None

class SafePlace(BaseModel):
    id: str
    name: str
    category: str  # POLICE, HOSPITAL, FIRE_STATION, SHELTER
    latitude: float
    longitude: float
    address: str
    distance_meters: float
    phone: Optional[str] = None

class SafePlacesResponse(BaseModel):
    latitude: float
    longitude: float
    total_found: int
    places: List[SafePlace]

class UserSettingsUpdate(BaseModel):
    shake_sos_enabled: Optional[bool] = None
    fall_detection_enabled: Optional[bool] = None
    voice_distress_enabled: Optional[bool] = None
    motion_detection_enabled: Optional[bool] = None
    evidence_recording_enabled: Optional[bool] = None
    location_sharing_enabled: Optional[bool] = None
    dark_mode_enabled: Optional[bool] = None
    emergency_contacts_only: Optional[bool] = None

class UserSettingsResponse(BaseModel):
    user_id: str
    shake_sos_enabled: bool = True
    fall_detection_enabled: bool = True
    voice_distress_enabled: bool = True
    motion_detection_enabled: bool = True
    evidence_recording_enabled: bool = True
    location_sharing_enabled: bool = True
    dark_mode_enabled: bool = True
    emergency_contacts_only: bool = False
    updated_at: str
