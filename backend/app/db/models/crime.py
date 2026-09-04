from datetime import datetime, timezone
from sqlalchemy import Column, String, Float, DateTime, Integer
from app.db.database import Base

class CrimeData(Base):
    __tablename__ = "crime_data"

    id = Column(Integer, primary_key=True, autoincrement=True)
    location_name = Column(String(100), nullable=True)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    crime_type = Column(String(50), nullable=False)
    occurred_at = Column(DateTime(timezone=True), nullable=True)
    severity = Column(Float, nullable=True, default=5.0)
    source = Column(String(50), nullable=True)
    created_at = Column(DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc))
