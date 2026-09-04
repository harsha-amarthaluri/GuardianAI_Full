from datetime import datetime, timezone
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

class HealthCheckResponse(BaseModel):
    status: str
    service: str
    version: str
    database: str
    timestamp: str
    disclaimer: str

@router.get("/health", response_model=HealthCheckResponse)
async def get_health():
    """
    Service health check endpoint.
    Verifies API server operational status and provides architecture baseline metadata.
    """
    return HealthCheckResponse(
        status="healthy",
        service="Guardian AI API",
        version="0.1.0",
        database="ready",
        timestamp=datetime.now(timezone.utc).isoformat(),
        disclaimer="Guardian AI provides threat estimates, not guaranteed safety."
    )
