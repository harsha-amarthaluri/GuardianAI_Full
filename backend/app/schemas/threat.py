from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, ConfigDict, Field, field_validator

class ThreatCreate(BaseModel):
    category: str = Field(..., json_schema_extra={"example": "CRIME"})
    severity: float = Field(..., ge=1.0, le=10.0, json_schema_extra={"example": 7.5})
    title: str = Field(..., max_length=150, json_schema_extra={"example": "High Crime Incident Reported"})
    description: Optional[str] = Field(None, max_length=500)
    latitude: float
    longitude: float
    radius: float = Field(500.0, ge=10.0, le=50000.0)
    source: Optional[str] = Field("SYSTEM", max_length=50)
    confidence: float = Field(0.85, ge=0.0, le=1.0)
    is_active: bool = True

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

class ThreatResponse(BaseModel):
    id: str
    category: str
    severity: float
    title: str
    description: Optional[str] = None
    latitude: float
    longitude: float
    radius: float
    timestamp: datetime
    source: Optional[str] = None
    confidence: float
    is_active: bool

    model_config = ConfigDict(from_attributes=True)

class ThreatListResponse(BaseModel):
    items: List[ThreatResponse]
    total: int
