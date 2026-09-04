# Guardian AI — Proactive Personal Safety Platform

> **Disclaimer & Safety Notice**: Guardian AI is a proactive personal safety platform designed to assist users with threat awareness and emergency alerting. **Guardian AI does NOT guarantee absolute safety.** Safety scores are mathematical estimates based on available historical and contextual data, not certainties. Automatic incident detection mechanisms (e.g., shake or fall detection) may produce false positives or false negatives. In an immediate life-threatening emergency, users should always contact official emergency services (e.g., 911 / 112) directly when possible.

---

## Overview

Guardian AI shifts personal safety from reactive emergency response to proactive threat awareness by integrating:
- **JWT-Based Secure User Authentication & Authorization**
- **Keystore-Backed Encrypted Android Token Persistence**
- **User Profile & Guardian Network Management**
- **Continuous Android Foreground Location Tracking Service**
- **Room ORM Offline Queue & Network Recovery Batch Upload**
- **Contextual Safety Score Evaluation UI & API**
- **Emergency SOS Incident Triggering & History Tracking**

---

## Monorepo Architecture

```
guardian-ai/
├── app/                # Native Java Android Mobile Application
│   └── app/src/main/java/com/guardianai/
│       ├── auth/       # TokenManager (Keystore-backed), SessionManager (401 expiration handling)
│       ├── data/       # ApiClient, GuardianApiService, DTOs & Repositories
│       │   ├── local/  # Room ORM (LocationEntity, LocationDao, AppDatabase)
│       │   ├── sync/   # SyncManager (batch upload), NetworkMonitor (NetworkCallback)
│       │   └── tracking/# LocationTrackingService, LocationProcessor, TrackingStateManager
│       ├── ui/         # SplashActivity, LoginActivity, RegisterActivity, MainActivity, Dialogs
│       └── utils/      # Client ValidationUtils
├── backend/            # FastAPI Python REST API & Microservices
│   ├── app/
│   │   ├── api/routes/ # Auth, Users, Guardians, Locations, Safety, SOS, Health
│   │   ├── core/       # Config, Security (bcrypt/JWT), Logging
│   │   ├── db/         # SQLAlchemy Database models & Session setup
│   │   ├── dependencies/# Auth & Database dependencies
│   │   ├── repositories/# Data access repositories
│   │   ├── schemas/    # Pydantic v2 validation schemas
│   │   └── services/   # Business logic & Notification stub
│   └── tests/          # Pytest unit & integration test suite
├── alembic/            # Alembic database migration scripts
├── admin/              # Admin Portal (User, Incident & Risk Management)
├── ml/                 # Machine Learning Models & Crime Risk Engine
├── data/               # Database DDL Schemas & Seed Data
├── docs/               # Architecture, API & Security Specifications
├── infrastructure/     # Docker & Deployment Infrastructure
├── scripts/            # Developer & Operational Helper Scripts
├── .env.example        # Environment Configuration Template
├── docker-compose.yml  # Local Development Multi-Container Setup
└── README.md           # Master Documentation
```

---

## Phase 3 Implemented Capabilities

- **Location Acquisition**: `LocationTrackingService` Android Foreground Service with ongoing notification (*"Guardian AI Protection Active"*) using `FusedLocationProviderClient` (30s update interval, 15s fastest interval, 20m minimum displacement).
- **Separation of Concerns**: Location acquisition decoupled from processing (`LocationProcessor`), local persistence (Room ORM), and synchronization (`SyncManager`).
- **Tracking State Machine**: `TrackingStateManager` (`STOPPED`, `STARTING`, `RUNNING`, `PAUSED`, `PERMISSION_REQUIRED`, `OFFLINE`, `ERROR`) notifying UI observers.
- **Local Room Offline Queue**: Persistent offline queue (`LocationEntity`, `LocationDao`, `AppDatabase`) storing status (`PENDING`, `UPLOADING`, `SYNCED`, `FAILED`, `FAILED_RETRY_DEFERRED`) with a 1000-record queue capacity limit.
- **Network Recovery Batch Sync**: `NetworkMonitor` auto-detecting network restoration and triggering `SyncManager` batch upload to `POST /api/v1/locations/batch` with max 5 bounded retries and deferred retry state.
- **Authoritative Server Deduplication**: FastAPI `POST /api/v1/locations/batch` with server-side duplicate suppression (ignores points for same user within 2s and < 5m displacement).
- **UI Tracking Controls**: Toggle Start/Stop tracking control card on Home Safety Dashboard with real-time status indicators.

---

## Quick Start & Setup Guide

### 1. Environment Setup
Copy the environment template:
```bash
cp .env.example .env
```

### 2. Run Backend Server
```powershell
# Create and activate virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1

# Install dependencies
pip install -r backend/requirements.txt

# Run Database Migrations
alembic -c backend/alembic.ini upgrade head

# Launch FastAPI Server
uvicorn backend.app.main:app --reload --port 8000
```
Backend will be live at `http://localhost:8000`.

### 3. Run Android Application
- Open the `app/` directory in **Android Studio**.
- Sync Gradle project dependencies (`build.gradle`).
- Run on **Android Emulator** or physical Android device.
- The app will automatically connect to `http://10.0.2.2:8000/` (Android Emulator loopback to host PC).

### 4. Running Backend Automated Tests
```powershell
.\venv\Scripts\python -m pytest backend/tests -v
```

---

## Render Hosting & 14-Minute Ping Keep-Alive Setup

### Deploying to Render
This repository includes a [`render.yaml`](render.yaml) Blueprint for zero-configuration deployment to [Render](https://render.com).

1. Connect your repository on Render.
2. Select **New > Blueprint** and select this repository.
3. Render automatically provisions the web service running `uvicorn backend.app.main:app --host 0.0.0.0 --port $PORT` with health checks on `/health`.

### 14-Minute Ping Keep-Alive Cron
Render's free tier spins down web services after 15 minutes of inactivity. To prevent cold starts and maintain 24/7 responsiveness:

- **GitHub Actions Cron Ping**: Automated workflow [`.github/workflows/keepalive.yml`](.github/workflows/keepalive.yml) runs every **14 minutes** (`*/14 * * * *`), sending an HTTP ping to keep the service warm. Set secret `RENDER_BACKEND_URL` in your GitHub repository settings.
- **Python Keep-Alive Script**: Run `python scripts/ping_keepalive.py --url https://<your-app>.onrender.com/health` as a daemon on any server or machine. Single pings can be tested with `--once`.

---

## Core Documentation

For detailed technical specifications, refer to the `docs/` directory:
- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — System Architecture, Subsystems, Data Flows
- [API_SPEC.md](docs/API_SPEC.md) — OpenAPI / REST Specifications
- [DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) — PostgreSQL / SQLite Data Models
- [DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md) — Phase-by-Phase Roadmap
- [SECURITY.md](docs/SECURITY.md) — Threat Model, Auth & Security Controls

---

## License

Copyright © 2026 Guardian AI Team. All rights reserved.
