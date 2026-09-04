from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

class WeatherDataResponse(BaseModel):
    latitude: float
    longitude: float
    temperature: float = Field(..., description="Temperature in Celsius")
    feels_like: Optional[float] = Field(None, description="Feels like temperature in Celsius")
    humidity: float = Field(..., description="Relative humidity percentage")
    wind_speed: float = Field(..., description="Wind speed in meters per second")
    visibility: float = Field(..., description="Visibility in meters")
    weather_condition: str = Field(..., description="Weather condition description")
    precipitation: float = Field(0.0, description="Precipitation rate in mm/h")
    timestamp: str = Field(..., description="ISO 8601 timestamp of data fetch")

class EnvironmentalRiskFactor(BaseModel):
    factor: str
    impact: str
    weight: float

class EnvironmentalRiskResponse(BaseModel):
    risk_level: str = Field(..., description="LOW, MODERATE, HIGH, or CRITICAL")
    risk_score: float = Field(..., description="Environmental risk score 0.0 - 100.0")
    risk_factors: List[EnvironmentalRiskFactor] = Field(default_factory=list)
    weather: WeatherDataResponse
    timestamp: str

class RouteAnalysisRequest(BaseModel):
    start_latitude: float = Field(..., ge=-90.0, le=90.0)
    start_longitude: float = Field(..., ge=-180.0, le=180.0)
    end_latitude: float = Field(..., ge=-90.0, le=90.0)
    end_longitude: float = Field(..., ge=-180.0, le=180.0)
    destination_name: Optional[str] = None

class CorridorOption(BaseModel):
    corridor_name: str
    distance_km: float
    nearby_threat_count: int
    threat_severity_sum: float
    risk_level: str
    description: str

class RouteAnalysisResponse(BaseModel):
    destination_name: str
    total_active_threats_in_region: int
    corridors: List[CorridorOption]
    recommended_corridor: str
    timestamp: str
