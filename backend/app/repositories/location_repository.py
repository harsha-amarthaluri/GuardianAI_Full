from typing import List, Optional
from sqlalchemy.orm import Session
from app.db.models.location import Location

class LocationRepository:
    def __init__(self, db: Session):
        self.db = db

    def create(self, location: Location) -> Location:
        self.db.add(location)
        self.db.commit()
        self.db.refresh(location)
        return location

    def get_user_locations(self, user_id: str, limit: int = 100) -> List[Location]:
        return self.db.query(Location).filter(
            Location.user_id == user_id
        ).order_by(Location.timestamp.desc()).limit(limit).all()
