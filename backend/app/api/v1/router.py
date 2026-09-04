from fastapi import APIRouter
from app.api.routes import auth, users, guardians, locations, safety, sos, health, threats, weather

api_router = APIRouter()

api_router.include_router(health.router, tags=["Health"])
api_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_router.include_router(users.router, prefix="/users", tags=["Users"])
api_router.include_router(guardians.router, prefix="/guardians", tags=["Guardians"])
api_router.include_router(locations.router, prefix="/locations", tags=["Location Telemetry"])
api_router.include_router(safety.router, prefix="/safety-score", tags=["Safety Score"])
api_router.include_router(threats.router, prefix="/threats", tags=["Threat Awareness"])
api_router.include_router(sos.router, prefix="/sos", tags=["SOS Incidents"])
api_router.include_router(weather.router, tags=["Weather & Environmental Risk"])
