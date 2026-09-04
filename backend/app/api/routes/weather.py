from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.weather import WeatherDataResponse, EnvironmentalRiskResponse
from app.services.weather_service import WeatherService
from app.services.environmental_risk_engine import EnvironmentalRiskEngine

router = APIRouter()

@router.get(
    "/weather",
    response_model=WeatherDataResponse,
    status_code=status.HTTP_200_OK,
    summary="Get live weather data for coordinates",
    description="Retrieves live weather indicators (temperature, humidity, wind speed, visibility) for given location."
)
def get_live_weather(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    current_user: User = Depends(get_current_user)
):
    service = WeatherService()
    return service.get_weather(latitude, longitude)

@router.get(
    "/environmental-risk",
    response_model=EnvironmentalRiskResponse,
    status_code=status.HTTP_200_OK,
    summary="Get environmental risk assessment",
    description="Evaluates weather indicators and computes real environmental risk score and risk factors."
)
def get_environmental_risk(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    current_user: User = Depends(get_current_user)
):
    service = WeatherService()
    engine = EnvironmentalRiskEngine()
    weather = service.get_weather(latitude, longitude)
    return engine.calculate_environmental_risk(weather)
