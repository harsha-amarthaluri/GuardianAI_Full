from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator

class LocationCreateRequest(BaseModel):
    latitude: float = Field(..., json_schema_extra={"example": 37.7749})
    longitude: float = Field(..., json_schema_extra={"example": -122.4194})
    accuracy: Optional[float] = Field(None, ge=0, json_schema_extra={"example": 5.0})
    timestamp: Optional[datetime] = None

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

class LocationResponse(BaseModel):
    id: int
    user_id: str
    latitude: float
    longitude: float
    accuracy: Optional[float] = None
    timestamp: datetime

    model_config = ConfigDict(from_attributes=True)

class LocationBatchCreateRequest(BaseModel):
    locations: List[LocationCreateRequest] = Field(..., min_length=1, max_length=50)

class LocationBatchResponse(BaseModel):
    processed_count: int
    ignored_duplicates_count: int
    items: List[LocationResponse]
