from datetime import datetime, timezone
from typing import Optional
from sqlalchemy.orm import Session
from app.schemas.safety import SafetyScoreResponse, LocationPoint, FactorDetail
from app.repositories.threat_repository import ThreatRepository
from app.services.weather_service import WeatherService
from app.services.environmental_risk_engine import EnvironmentalRiskEngine

class SafetyScoreService:
    """
    SafetyScoreService evaluates situational safety scores (0-100) based on
    spatial threat proximity, environmental weather risks, time of day, and location context.
    """
    def __init__(self):
        self.weather_service = WeatherService()
        self.environmental_engine = EnvironmentalRiskEngine()

    def calculate_score(self, latitude: float, longitude: float, db: Optional[Session] = None) -> SafetyScoreResponse:
        score = 100.0
        factors = []

        # 1. Evaluate nearby database threats if DB session is available
        if db is not None:
            try:
                repo = ThreatRepository(db)
                nearby_threats = repo.get_active_threats(latitude, longitude, radius_meters=5000.0)

                for threat in nearby_threats:
                    dist = repo.haversine_distance(latitude, longitude, threat.latitude, threat.longitude)
                    decay = max(0.2, 1.0 - (dist / 5000.0))
                    impact = min(25.0, threat.severity * decay * 2.5)

                    score -= impact
                    factors.append(FactorDetail(
                        factor=f"threat_{threat.category.lower()}",
                        impact=f"{threat.title} ({int(dist)}m away)",
                        weight=round(impact, 1)
                    ))
            except Exception:
                pass

        # 2. Evaluate Live Environmental Weather Risk
        try:
            weather_data = self.weather_service.get_weather(latitude, longitude)
            env_risk = self.environmental_engine.calculate_environmental_risk(weather_data)

            if env_risk.risk_score > 0:
                env_penalty = env_risk.risk_score * 0.25 # Scale factor impact
                score -= env_penalty
                for f in env_risk.risk_factors:
                    if f.weight > 0:
                        factors.append(FactorDetail(
                            factor=f"env_{f.factor}",
                            impact=f.impact,
                            weight=round(f.weight * 0.25, 1)
                        ))
            else:
                factors.append(FactorDetail(
                    factor="environment",
                    impact=f"Normal weather ({weather_data.weather_condition}, {weather_data.temperature}°C)",
                    weight=0.0
                ))
        except Exception:
            pass

        # 3. Time-of-Day Risk Factor (Night penalty between 22:00 and 05:00 UTC)
        current_hour = datetime.now(timezone.utc).hour
        if current_hour >= 22 or current_hour < 5:
            night_penalty = 10.0
            score -= night_penalty
            factors.append(FactorDetail(
                factor="time_of_day",
                impact="Late night hours risk coefficient",
                weight=night_penalty
            ))

        # 4. High Latitude / Sparse Coverage Factor
        if abs(latitude) > 50.0:
            zone_penalty = 5.0
            score -= zone_penalty
            factors.append(FactorDetail(
                factor="geographic_zone",
                impact="Remote or high-latitude zone indicator",
                weight=zone_penalty
            ))

        # Clamp final score between 0.0 and 100.0
        final_score = max(0.0, min(100.0, score))

        # Determine Risk Category
        if final_score >= 80.0:
            category = "LOW"
        elif final_score >= 60.0:
            category = "MODERATE"
        elif final_score >= 40.0:
            category = "HIGH"
        else:
            category = "CRITICAL"

        return SafetyScoreResponse(
            score=round(final_score, 1),
            category=category,
            location=LocationPoint(latitude=latitude, longitude=longitude),
            factors=factors,
            disclaimer="Safety score is a situational awareness estimate based on contextual indicators. It does NOT guarantee user safety."
        )
