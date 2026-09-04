from typing import List, Optional
from pydantic import BaseModel, Field, field_validator

class FactorDetail(BaseModel):
    factor: str = Field(..., json_schema_extra={"example": "time_of_day"})
    impact: str = Field(..., json_schema_extra={"example": "night_hours_penalty"})
    weight: float = Field(..., json_schema_extra={"example": 6.3})

class LocationPoint(BaseModel):
    latitude: float
    longitude: float

class SafetyScoreQuery(BaseModel):
    latitude: float
    longitude: float

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

class SafetyScoreResponse(BaseModel):
    score: float = Field(..., ge=0.0, le=100.0, json_schema_extra={"example": 75.5})
    category: str = Field(..., json_schema_extra={"example": "MODERATE"})
    location: LocationPoint
    factors: List[FactorDetail] = []
    disclaimer: str = "Safety score is an estimate based on contextual indicators. It does not guarantee safety."
