import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship as rel
from app.db.database import Base

def generate_uuid():
    return str(uuid.uuid4())

class SOSIncident(Base):
    __tablename__ = "sos_incidents"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    trigger_type = Column(String(30), nullable=False)
    risk_score = Column(Float, nullable=True)
    status = Column(String(20), nullable=False, default="DETECTED")
    created_at = Column(DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))
    resolved_at = Column(DateTime(timezone=True), nullable=True)

    user = rel("User", back_populates="sos_incidents")
