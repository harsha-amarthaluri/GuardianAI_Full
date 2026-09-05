import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship as rel
from app.db.database import Base

def generate_uuid():
    return str(uuid.uuid4())

class SOSEvent(Base):
    __tablename__ = "sos_incident_events"

    id = Column(String(36), primary_key=True, default=generate_uuid)
    sos_id = Column(String(36), ForeignKey("sos_incidents.id", ondelete="CASCADE"), nullable=False, index=True)
    event_type = Column(String(100), nullable=False)
    actor_type = Column(String(50), nullable=False, default="SYSTEM")
    status = Column(String(50), nullable=False, default="SUCCESS")
    details = Column(JSON, nullable=True, default=dict)
    created_at = Column(DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc))

    sos_incident = rel("SOSIncident")
