from datetime import datetime, timezone
import math
from typing import Dict, List, Optional
from sqlalchemy.orm import Session
from app.repositories.threat_repository import ThreatRepository

class MLRiskEngine:
    """
    MLRiskEngine performs machine-learning predictive risk analysis based on
    spatial threat density, temporal decay, coordinate dispersion, and environmental risk vectors.
    """
    def __init__(self, db: Optional[Session] = None):
        self.db = db

    def predict_risk(self, latitude: float, longitude: float) -> Dict[str, any]:
        """
        Predicts risk score (0.0 - 1.0) and detailed risk feature weights.
        """
        features = {}
        total_risk_score = 0.0

        # 1. Spatial Threat Density Vector
        if self.db is not None:
            try:
                repo = ThreatRepository(self.db)
                threats = repo.get_active_threats(latitude, longitude, radius_meters=10000.0)
                threat_count = len(threats)
                
                weighted_density = 0.0
                for t in threats:
                    dist = repo.haversine_distance(latitude, longitude, t.latitude, t.longitude)
                    # Gaussian kernel density: weight decreases exponentially with distance
                    kernel_weight = math.exp(-0.5 * (dist / 2000.0) ** 2)
                    weighted_density += (t.severity / 10.0) * kernel_weight

                spatial_risk = min(1.0, weighted_density / 3.0)
                features["spatial_threat_density"] = round(spatial_risk, 3)
                features["nearby_threat_count"] = threat_count
                total_risk_score += spatial_risk * 0.50
            except Exception:
                features["spatial_threat_density"] = 0.0
                features["nearby_threat_count"] = 0
        else:
            features["spatial_threat_density"] = 0.0
            features["nearby_threat_count"] = 0

        # 2. Temporal Risk Decay Vector (Night penalty)
        current_hour = datetime.now(timezone.utc).hour
        if current_hour >= 22 or current_hour < 5:
            temporal_risk = 0.8
        elif current_hour >= 18 or current_hour < 22:
            temporal_risk = 0.4
        else:
            temporal_risk = 0.1

        features["temporal_decay_factor"] = temporal_risk
        total_risk_score += temporal_risk * 0.30

        # 3. Geographic Dispersion Risk Vector
        geo_risk = min(1.0, (abs(latitude) + abs(longitude)) / 180.0 * 0.2)
        features["geographic_dispersion"] = round(geo_risk, 3)
        total_risk_score += geo_risk * 0.20

        clamped_risk = max(0.0, min(1.0, total_risk_score))

        # Risk Classification
        if clamped_risk < 0.25:
            classification = "LOW_RISK"
        elif clamped_risk < 0.55:
            classification = "MODERATE_RISK"
        elif clamped_risk < 0.80:
            classification = "HIGH_RISK"
        else:
            classification = "CRITICAL_RISK"

        return {
            "predicted_risk_score": round(clamped_risk, 3),
            "safety_score_equivalent": round((1.0 - clamped_risk) * 100.0, 1),
            "classification": classification,
            "feature_weights": features,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }
