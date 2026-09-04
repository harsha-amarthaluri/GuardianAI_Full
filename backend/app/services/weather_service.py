import logging
import time
import urllib.request
import json
from datetime import datetime, timezone
from typing import Dict, Optional, Tuple

from app.core.config import settings
from app.schemas.weather import WeatherDataResponse, EnvironmentalRiskResponse, EnvironmentalRiskFactor

logger = logging.getLogger("guardian_ai")

# In-memory weather cache (key: round(lat, 2)_round(lon, 2), value: (timestamp, WeatherDataResponse))
WEATHER_CACHE: Dict[str, Tuple[float, WeatherDataResponse]] = {}
CACHE_TTL_SECONDS = 600.0  # 10 minutes cache TTL to prevent rate limits and unnecessary network calls

class WeatherService:
    """
    Real Weather Service fetching live meteorological data from Open-Meteo (free public provider)
    or OpenWeatherMap (if OPENWEATHER_API_KEY is configured in settings).
    """

    def get_weather(self, latitude: float, longitude: float) -> WeatherDataResponse:
        cache_key = f"{round(latitude, 2)}_{round(longitude, 2)}"
        now = time.time()

        if cache_key in WEATHER_CACHE:
            cached_time, cached_data = WEATHER_CACHE[cache_key]
            if now - cached_time < CACHE_TTL_SECONDS:
                logger.info(f"🌤 Using cached weather data for {cache_key}")
                return cached_data

        # Attempt fetching real live weather
        weather_data = None
        if settings.OPENWEATHER_API_KEY:
            weather_data = self._fetch_openweather(latitude, longitude)

        if weather_data is None:
            weather_data = self._fetch_open_meteo(latitude, longitude)

        if weather_data is None:
            raise RuntimeError("Live weather provider unavailable or network error")

        WEATHER_CACHE[cache_key] = (now, weather_data)
        return weather_data

    def _fetch_open_meteo(self, latitude: float, longitude: float) -> Optional[WeatherDataResponse]:
        try:
            url = f"https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current_weather=true&hourly=relativehumidity_2m,visibility"
            req = urllib.request.Request(url, headers={"User-Agent": "GuardianAI/1.0"})
            with urllib.request.urlopen(req, timeout=5) as response:
                if response.status == 200:
                    data = json.loads(response.read().decode("utf-8"))
                    current = data.get("current_weather", {})
                    temp = current.get("temperature", 20.0)
                    wind = current.get("windspeed", 3.0) # km/h or m/s depending on API
                    weather_code = current.get("weathercode", 0)

                    # Map weather codes
                    condition = self._map_wmo_code(weather_code)

                    return WeatherDataResponse(
                        latitude=latitude,
                        longitude=longitude,
                        temperature=temp,
                        feels_like=temp,
                        humidity=50.0,
                        wind_speed=round(wind / 3.6, 1), # convert km/h to m/s
                        visibility=10000.0,
                        weather_condition=condition,
                        precipitation=0.0,
                        timestamp=datetime.now(timezone.utc).isoformat()
                    )
        except Exception as e:
            logger.warning(f"Failed to fetch Open-Meteo weather: {e}")
        return None

    def _fetch_openweather(self, latitude: float, longitude: float) -> Optional[WeatherDataResponse]:
        try:
            url = f"https://api.openweathermap.org/data/2.5/weather?lat={latitude}&lon={longitude}&appid={settings.OPENWEATHER_API_KEY}&units=metric"
            req = urllib.request.Request(url, headers={"User-Agent": "GuardianAI/1.0"})
            with urllib.request.urlopen(req, timeout=5) as response:
                if response.status == 200:
                    data = json.loads(response.read().decode("utf-8"))
                    main = data.get("main", {})
                    wind = data.get("wind", {})
                    weather_list = data.get("weather", [{}])

                    return WeatherDataResponse(
                        latitude=latitude,
                        longitude=longitude,
                        temperature=main.get("temp", 20.0),
                        feels_like=main.get("feels_like", 20.0),
                        humidity=main.get("humidity", 50.0),
                        wind_speed=wind.get("speed", 2.0),
                        visibility=float(data.get("visibility", 10000.0)),
                        weather_condition=weather_list[0].get("description", "Clear").capitalize(),
                        precipitation=0.0,
                        timestamp=datetime.now(timezone.utc).isoformat()
                    )
        except Exception as e:
            logger.warning(f"Failed to fetch OpenWeatherMap: {e}")
        return None

    def _map_wmo_code(self, code: int) -> str:
        if code == 0: return "Clear sky"
        if code in [1, 2, 3]: return "Partly cloudy"
        if code in [45, 48]: return "Foggy"
        if code in [51, 53, 55]: return "Drizzle"
        if code in [61, 63, 65]: return "Rain"
        if code in [71, 73, 75]: return "Snow"
        if code in [80, 81, 82]: return "Rain showers"
        if code in [95, 96, 99]: return "Thunderstorm"
        return "Overcast"
