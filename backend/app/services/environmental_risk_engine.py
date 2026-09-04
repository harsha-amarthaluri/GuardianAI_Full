from datetime import datetime, timezone
from typing import List
from app.schemas.weather import WeatherDataResponse, EnvironmentalRiskResponse, EnvironmentalRiskFactor

class EnvironmentalRiskEngine:
    """
    EnvironmentalRiskEngine evaluates weather indicators (visibility, precipitation, wind speed, temperature extremes)
    to calculate real environmental risk score and risk level.
    """
    def calculate_environmental_risk(self, weather: WeatherDataResponse) -> EnvironmentalRiskResponse:
        risk_score = 0.0
        factors: List[EnvironmentalRiskFactor] = []

        # 1. Visibility Risk Vector
        if weather.visibility < 1000.0:
            v_impact = 35.0
            risk_score += v_impact
            factors.append(EnvironmentalRiskFactor(
                factor="visibility",
                impact=f"Severe fog/low visibility ({int(weather.visibility)}m)",
                weight=v_impact
            ))
        elif weather.visibility < 5000.0:
            v_impact = 15.0
            risk_score += v_impact
            factors.append(EnvironmentalRiskFactor(
                factor="visibility",
                impact=f"Reduced visibility ({int(weather.visibility)}m)",
                weight=v_impact
            ))

        # 2. Wind Speed Risk Vector
        if weather.wind_speed > 15.0: # > 54 km/h
            w_impact = 30.0
            risk_score += w_impact
            factors.append(EnvironmentalRiskFactor(
                factor="wind_speed",
                impact=f"High wind speed ({weather.wind_speed} m/s)",
                weight=w_impact
            ))
        elif weather.wind_speed > 8.0:
            w_impact = 10.0
            risk_score += w_impact
            factors.append(EnvironmentalRiskFactor(
                factor="wind_speed",
                impact=f"Moderate wind speed ({weather.wind_speed} m/s)",
                weight=w_impact
            ))

        # 3. Weather Condition / Rain Risk Vector
        cond_lower = weather.weather_condition.lower()
        if "thunderstorm" in cond_lower:
            c_impact = 40.0
            risk_score += c_impact
            factors.append(EnvironmentalRiskFactor(
                factor="weather_condition",
                impact="Severe thunderstorm alert",
                weight=c_impact
            ))
        elif "rain" in cond_lower or "shower" in cond_lower:
            c_impact = 20.0
            risk_score += c_impact
            factors.append(EnvironmentalRiskFactor(
                factor="weather_condition",
                impact=f"Rainfall: {weather.weather_condition}",
                weight=c_impact
            ))
        elif "snow" in cond_lower or "fog" in cond_lower:
            c_impact = 25.0
            risk_score += c_impact
            factors.append(EnvironmentalRiskFactor(
                factor="weather_condition",
                impact=f"Adverse weather condition: {weather.weather_condition}",
                weight=c_impact
            ))

        # 4. Temperature Extremes Vector
        if weather.temperature < 0.0:
            t_impact = 20.0
            risk_score += t_impact
            factors.append(EnvironmentalRiskFactor(
                factor="temperature",
                impact=f"Freezing temperature ({weather.temperature}°C)",
                weight=t_impact
            ))
        elif weather.temperature > 40.0:
            t_impact = 20.0
            risk_score += t_impact
            factors.append(EnvironmentalRiskFactor(
                factor="temperature",
                impact=f"Extreme heat ({weather.temperature}°C)",
                weight=t_impact
            ))

        clamped_score = min(100.0, max(0.0, risk_score))

        if clamped_score >= 60.0:
            level = "HIGH"
        elif clamped_score >= 30.0:
            level = "MODERATE"
        else:
            level = "LOW"

        if not factors:
            factors.append(EnvironmentalRiskFactor(
                factor="normal_conditions",
                impact="Normal environmental conditions",
                weight=0.0
            ))

        return EnvironmentalRiskResponse(
            risk_level=level,
            risk_score=round(clamped_score, 1),
            risk_factors=factors,
            weather=weather,
            timestamp=datetime.now(timezone.utc).isoformat()
        )
