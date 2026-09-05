from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, ConfigDict, Field

class JourneyStartRequest(BaseModel):
    origin_latitude: float = Field(..., ge=-90.0, le=90.0)
    origin_longitude: float = Field(..., ge=-180.0, le=180.0)
    destination_address: Optional[str] = None
    destination_latitude: float = Field(..., ge=-90.0, le=90.0)
    destination_longitude: float = Field(..., ge=-180.0, le=180.0)
    expected_arrival_time: datetime

class JourneyPingRequest(BaseModel):
    current_latitude: float = Field(..., ge=-90.0, le=90.0)
    current_longitude: float = Field(..., ge=-180.0, le=180.0)

class JourneyResponse(BaseModel):
    id: str
    user_id: str
    origin_latitude: float
    origin_longitude: float
    destination_address: Optional[str] = None
    destination_latitude: float
    destination_longitude: float
    expected_arrival_time: datetime
    status: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)

class JourneyListResponse(BaseModel):
    items: List[JourneyResponse]
    total: int
    skip: int
    limit: int
