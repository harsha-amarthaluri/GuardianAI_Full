from typing import List, Optional
from sqlalchemy.orm import Session
from app.db.models.guardian import Guardian

class GuardianRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_user_id(self, user_id: str) -> List[Guardian]:
        return self.db.query(Guardian).filter(Guardian.user_id == user_id).all()

    def get_by_id_and_user_id(self, guardian_id: str, user_id: str) -> Optional[Guardian]:
        return self.db.query(Guardian).filter(
            Guardian.id == guardian_id,
            Guardian.user_id == user_id
        ).first()

    def create(self, guardian: Guardian) -> Guardian:
        self.db.add(guardian)
        self.db.commit()
        self.db.refresh(guardian)
        return guardian

    def update(self, guardian: Guardian) -> Guardian:
        self.db.commit()
        self.db.refresh(guardian)
        return guardian

    def delete(self, guardian: Guardian) -> None:
        self.db.delete(guardian)
        self.db.commit()
