from datetime import datetime
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator

class TriggerTypeEnum(str, Enum):
    MANUAL = "MANUAL"
    SHAKE = "SHAKE"
    STILLNESS = "STILLNESS"
    VOICE = "VOICE"
    SYSTEM = "SYSTEM"

class IncidentStatusEnum(str, Enum):
    DETECTED = "DETECTED"
    ALERTING = "ALERTING"
    ACKNOWLEDGED = "ACKNOWLEDGED"
    RESOLVED = "RESOLVED"
    FALSE_ALARM = "FALSE_ALARM"
    FAILED = "FAILED"

class SOSCreateRequest(BaseModel):
    latitude: float = Field(..., json_schema_extra={"example": 37.7749})
    longitude: float = Field(..., json_schema_extra={"example": -122.4194})
    trigger_type: TriggerTypeEnum = Field(..., json_schema_extra={"example": TriggerTypeEnum.MANUAL})

    @field_validator("latitude")
    @classmethod
    def validate_latitude(cls, v: float) -> float:
        if not (-90.0 <= v <= 90.0):
            raise ValueError("Latitude must be between -90.0 and 90.0")
        return v

    @field_validator("longitude")
    @classmethod
    def validate_longitude(cls, v: float) -> float:
        if not (-180.0 <= v <= 180.0):
            raise ValueError("Longitude must be between -180.0 and 180.0")
        return v

class SOSResponse(BaseModel):
    id: str
    user_id: str
    latitude: float
    longitude: float
    trigger_type: str
    risk_score: Optional[float] = None
    status: str
    created_at: datetime
    updated_at: datetime
    resolved_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)

class SOSListResponse(BaseModel):
    items: List[SOSResponse]
    total: int
    skip: int
    limit: int
