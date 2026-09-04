from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session
from typing import Dict, Any

from app.db.database import get_db
from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.safety import SafetyScoreResponse, SafetyScoreQuery
from app.schemas.weather import RouteAnalysisRequest, RouteAnalysisResponse
from app.services.safety_service import SafetyScoreService
from app.services.ml_risk_engine import MLRiskEngine
from app.services.route_analysis_service import RouteAnalysisService

router = APIRouter()

@router.get(
    "",
    response_model=SafetyScoreResponse,
    status_code=status.HTTP_200_OK,
    summary="Get safety score for location",
    description="Calculates contextual safety score and risk category for specified geographic coordinates."
)
def get_safety_score(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = SafetyScoreQuery(latitude=latitude, longitude=longitude)
    service = SafetyScoreService()
    return service.calculate_score(query.latitude, query.longitude, db)

@router.get(
    "/predict-risk",
    status_code=status.HTTP_200_OK,
    summary="Predict ML situational risk",
    description="Evaluates spatial threat density, temporal decay, and geographic dispersion vectors using ML model."
)
def predict_risk(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    engine = MLRiskEngine(db=db)
    return engine.predict_risk(latitude, longitude)

@router.post(
    "/route-analysis",
    response_model=RouteAnalysisResponse,
    status_code=status.HTTP_200_OK,
    summary="Analyze route threat corridor risk",
    description="Evaluates candidate route corridors against nearby active spatial threats, distance, and severity."
)
def analyze_route(
    request: RouteAnalysisRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service = RouteAnalysisService(db)
    return service.analyze_route(request)
