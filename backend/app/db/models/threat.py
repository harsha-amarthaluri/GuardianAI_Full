import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Float, Boolean, DateTime, Integer
from app.db.database import Base

def generate_uuid():
    return str(uuid.uuid4())

class Threat(Base):
    __tablename__ = "threats"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    category = Column(String(50), nullable=False, index=True) # CRIME, WEATHER, ENVIRONMENTAL, LOCATION_RISK, TIME_RISK, USER_REPORTED, EMERGENCY
    severity = Column(Float, nullable=False, default=5.0) # 1.0 (lowest) to 10.0 (highest)
    title = Column(String(150), nullable=False)
    description = Column(String(500), nullable=True)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    radius = Column(Float, nullable=False, default=500.0) # Affected radius in meters
    timestamp = Column(DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc))
    source = Column(String(50), nullable=True, default="SYSTEM")
    confidence = Column(Float, nullable=False, default=0.85) # 0.0 to 1.0
    is_active = Column(Boolean, nullable=False, default=True)
