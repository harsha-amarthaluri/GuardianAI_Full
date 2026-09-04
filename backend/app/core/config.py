import os
from typing import List, Union
from pydantic import AnyHttpUrl, validator
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    PROJECT_NAME: str = "Guardian AI"
    API_V1_STR: str = "/api/v1"
    ENVIRONMENT: str = "development"
    DEBUG: bool = True

    # Security
    SECRET_KEY: str = "DEV_ONLY_SECRET_KEY_CHANGE_IN_PRODUCTION_123456789"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440

    # Optional External Weather Service API Key
    OPENWEATHER_API_KEY: str = ""

    # CORS Configurable
    BACKEND_CORS_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:8000",
        "http://127.0.0.1:8000",
    ]

    # Database Settings (Default to SQLite for local ease, override via DATABASE_URL for Postgres)
    DATABASE_URL: str = "sqlite:///./guardian.db"

    _env_file_path: str = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".env"))
    model_config = SettingsConfigDict(env_file=(_env_file_path, ".env"), case_sensitive=True, extra="allow")

settings = Settings()
