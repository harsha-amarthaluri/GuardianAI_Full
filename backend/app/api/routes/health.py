from datetime import datetime, timezone
from fastapi import APIRouter, Depends
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.db.database import get_db

router = APIRouter()

@router.get(
    "/health",
    summary="Health and database readiness check",
    description="Returns backend operational status and database connection readiness status."
)
def get_health(db: Session = Depends(get_db)):
    db_status = "healthy"
    try:
        db.execute(text("SELECT 1"))
    except Exception as e:
        db_status = f"unhealthy: {str(e)}"

    return {
        "status": "healthy",
        "service": "Guardian AI API",
        "version": "0.1.0",
        "database": db_status,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "disclaimer": "Guardian AI provides threat estimates, not guaranteed safety."
    }
