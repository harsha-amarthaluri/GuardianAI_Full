from pydantic import BaseModel
from typing import Optional, Dict, Any
from datetime import datetime

class SOSEventCreate(BaseModel):
    sos_id: str
    event_type: str
    actor_type: Optional[str] = "SYSTEM"
    status: Optional[str] = "SUCCESS"
    details: Optional[Dict[str, Any]] = None

class SOSEventResponse(BaseModel):
    id: str
    sos_id: str
    event_type: str
    actor_type: str
    status: str
    details: Optional[Dict[str, Any]] = None
    created_at: datetime

    class Config:
        from_attributes = True
