# Guardian AI — System Architecture & Technical Design

## 1. Executive Summary & Vision

**Guardian AI** is a proactive personal safety platform designed to transform personal safety from reactive emergency responses into proactive threat awareness. By fusing user location telemetry, historical and real-time crime data, contextual environmental signals, motion sensors, and machine learning risk models, Guardian AI delivers real-time safety scores, safe route navigation, dynamic guardian alerts, and emergency SOS broadcasting.

> **CRITICAL DISCLAIMER**: Guardian AI provides situational awareness estimates. It does NOT guarantee user safety under any circumstances. All safety scores and threat indicators are statistical predictions and approximations.

---

## 2. Monorepo Architecture Overview

Guardian AI is organized as a clean, modular monorepo:

```
c:/GuardianAi/
├── app/                # Native Java Android App (MVVM architecture)
├── backend/            # FastAPI async REST microservices
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
├── admin/              # Admin control panel (User, Incident, Data Admin)
├── ml/                 # Risk scoring & threat prediction models
├── data/               # Relational & Geospatial PostgreSQL/PostGIS DDL
├── docs/               # Technical specs & architecture contracts
├── infrastructure/     # Container orchestration & CI/CD deployment
└── scripts/            # Operational & automation scripts
```

---

## 3. High-Level Component Interaction Diagram

```mermaid
graph TD
    subgraph Mobile Client [Android App - Java]
        UI[Safety Dashboard & Map UI]
        GPS[Location Manager]
        HTTPClient[Retrofit / OkHttp Client]
    end

    subgraph Backend Microservice [FastAPI Python]
        AuthAPI[Auth Service - bcrypt / JWT]
        UserAPI[User Profile Service]
        GuardianAPI[Guardian Network Service]
        LocationAPI[Location Telemetry API]
        SafetyEngine[Safety Score Service Abstract Interface]
        SOSEngine[SOS Incident Service & Notification Stub]
    end

    subgraph Database Layer
        DB[(PostgreSQL / SQLite Database)]
        Alembic[Alembic Migrations]
    end

    GPS -->|Lat, Lon, Accuracy| HTTPClient
    UI -->|Auth & Resource Requests| HTTPClient

    HTTPClient -->|HTTPS REST / Bearer JWT| Backend Microservice

    AuthAPI --> DB
    UserAPI --> DB
    GuardianAPI --> DB
    LocationAPI --> DB
    SafetyEngine --> DB
    SOSEngine --> DB
```

---

## 4. Subsystem Specifications

### 4.1 Backend Foundation (`backend/`)
- **Language & Framework**: Python 3.10+, FastAPI, Pydantic v2.
- **ORM & Migrations**: SQLAlchemy 2.0 + Alembic.
- **Key Modules**:
  - `Core Security`: Password hashing (`bcrypt`), JWT Bearer token generation and decoding (`pyjwt`).
  - `Auth API`: OAuth2 JWT auth, registration (`POST /api/v1/auth/register`), login (`POST /api/v1/auth/login`), profile (`GET /api/v1/auth/me`).
  - `User API`: Profile management (`GET`, `PUT /api/v1/users/me`).
  - `Guardian API`: Full CRUD (`/api/v1/guardians`) with user authorization isolation.
  - `Location API`: Ingestion of client GPS telemetry (`POST /api/v1/locations`).
  - `Safety API`: Rule-based placeholder service abstraction (`SafetyScoreService`).
  - `SOS API`: Emergency incident trigger (`POST /api/v1/sos`) and history (`GET /api/v1/sos`).
  - `Notification Stub`: `NotificationServiceStub` interface logging alert events for future provider integration.

### 4.2 Database Layer (`alembic/` & `backend/app/db/`)
- **Engine**: SQLite for fast local testing and PostgreSQL for production.
- **Core Entities**: `User`, `Guardian`, `Location`, `SafetyScore`, `SOSIncident`, `CrimeData`.

---

## 5. Architectural Principles & Guidelines

1. **Modular Decoupling**: Business logic, API endpoints, data persistence, and UI are strictly separated using service and repository layers.
2. **Explicit Contracts**: OpenAPI / Pydantic v2 schemas enforce type safety and parameter validation across all endpoints.
3. **No Hardcoded Secrets**: Secret keys, database connection strings, and API keys are injected via environment variables (`.env`).
4. **Strict Resource Isolation**: Every user-owned resource is scoped to the authenticated user derived from the JWT access token.
