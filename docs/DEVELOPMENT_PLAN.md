# Guardian AI — Phase-by-Phase Development Roadmap

## Phase Roadmap Overview

| Phase | Module | Primary Objectives | Status |
| :--- | :--- | :--- | :--- |
| **Phase 0** | **Architecture & Foundation** | Baseline setup, docs, Docker, DB schema, health check, Android shell | **COMPLETED** |
| **Phase 1** | **Backend Foundation** | JWT auth, User profile, Guardian CRUD, Location API, Safety score, SOS API | **COMPLETED** |
| **Phase 2** | **Android Foundation & Integration** | Android app architecture, Keystore token storage, Login/Register UI, Safety dashboard, Guardians UI, SOS alert UI | **COMPLETED** |
| **Phase 3** | **Location Telemetry & Tracking** | Android Foreground Service, FusedLocationProviderClient, Room DB offline queue, SyncManager network batch recovery (`POST /api/v1/locations/batch`), TrackingStateManager | **COMPLETED** |
| **Phase 4** | **Crime & Safety Scoring** | Crime data pipeline, spatial density scoring engine | Planned |
| **Phase 5** | **Safe Routing & Maps** | OpenStreetMap integration, safe route calculation | Planned |
| **Phase 6** | **SOS & Alert Dispatch** | Real SMS/Push notifications (Twilio/FCM), guardian alert broadcast | Planned |
| **Phase 7** | **Sensor Engine** | Accelerometer shake/fall detection algorithms | Planned |
| **Phase 8** | **Admin Portal** | Incident dashboard, crime data management | Planned |
| **Phase 9** | **Advanced ML & Evaluation** | Machine learning risk predictive models & validation | Planned |

---

## Detailed Phase Accomplishments

### Phase 0: Master Architecture & Foundation Setup (COMPLETED)
- [x] Establish monorepo workspace directory structure.
- [x] Write architectural, database schema, API spec, and security specifications.
- [x] Create FastAPI backend skeleton with Pydantic configuration and structured logging.

### Phase 1: Backend Foundation (COMPLETED)
- [x] **Database & Migrations**: SQLAlchemy ORM models (`User`, `Guardian`, `Location`, `SafetyScore`, `SOSIncident`, `CrimeData`) and Alembic migration `001_initial_schema`.
- [x] **Authentication**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me`. Passwords hashed with `bcrypt`.
- [x] **Authorization & Isolation**: `get_current_user` dependency enforcing resource isolation across users.
- [x] **APIs**: User Profile (`/users/me`), Guardians (`/guardians`), Locations (`/locations`), Safety Score (`/safety-score`), SOS Incidents (`/sos`), Health (`/health`).
- [x] **Automated Tests**: 22 pytest backend tests passing with 100% pass rate.

### Phase 2: Android Foundation & Integration (COMPLETED)
- [x] **Android Architecture**: Separated into Data Layer (Retrofit, OkHttp, Gson DTOs, Repositories), Auth Layer (`TokenManager`, `SessionManager`), and UI Controllers.
- [x] **Keystore Token Storage**: `TokenManager` utilizing `EncryptedSharedPreferences` for secure storage of JWT access tokens.
- [x] **Centralized 401 Expiration**: OkHttp interceptor catching 401 Unauthorized responses and delegating to `SessionManager` to clear session and navigate to Login.
- [x] **Splash & Auth Screens**: `SplashActivity` session routing, `LoginActivity` credential validation, `RegisterActivity` form validation.
- [x] **Home Dashboard & Manual SOS**: `MainActivity` tab host displaying Safety Score (85 LOW RISK with disclaimer), location status & manual telemetry submission, and prominent **Manual SOS Alert Button** with 3-second safety countdown confirmation dialog.
- [x] **Guardian CRUD UI**: Guardian listing, add guardian dialog, edit guardian dialog, delete guardian with confirmation.
- [x] **Profile & Incident History**: User profile view/edit and paginated SOS incident history.

### Phase 3: Location Telemetry & Background Tracking (COMPLETED)
- [x] **Android Location Acquisition**: Implemented `LocationTrackingService` (Android Foreground Service with ongoing notification *"Guardian AI Protection Active"*) utilizing `FusedLocationProviderClient` configured with 30s update interval, 15s fastest interval, and 20m minimum displacement.
- [x] **Separation of Concerns**: Kept service focused strictly on location acquisition; decoupled processing into `LocationProcessor`, local storage into Room DAO, and synchronization into `SyncManager`.
- [x] **Tracking State Management**: Centralized `TrackingStateManager` (`STOPPED`, `STARTING`, `RUNNING`, `PAUSED`, `PERMISSION_REQUIRED`, `OFFLINE`, `ERROR`) notifying UI observers.
- [x] **Room Local Offline Queue**: Implemented Room Database (`LocationEntity`, `LocationDao`, `AppDatabase`) storing locations with status (`PENDING`, `UPLOADING`, `SYNCED`, `FAILED`, `FAILED_RETRY_DEFERRED`) and 1000-record queue retention capacity.
- [x] **Network Monitor & Batch Sync**: Implemented `NetworkMonitor` (Android `NetworkCallback`) auto-triggering `SyncManager` upon network restoration to perform batch upload (`POST /api/v1/locations/batch`).
- [x] **Bounded Retry & Deferred State**: Exponential backoff retry policy with max 5 retries, marking failed records as `FAILED_RETRY_DEFERRED`.
- [x] **Server-Side Deduplication**: Added authoritative server-side duplicate suppression (ignores points for same user within 2s and < 5m displacement).
- [x] **UI Tracking Controls**: Added tracking status card and toggle ON/OFF controls to Home Safety Dashboard.
- [x] **Automated Tests**: 24/24 pytest backend tests passing, JUnit Android tests (`TrackingStateTest`, `LocationQueueStateTest`, `ValidationTest`, `ModelParsingTest`) passing.

---

## Next Recommended Phase

**Phase 4: Crime Data Ingestion & Spatial Safety Scoring**
- Implement spatial density scoring algorithm using historical crime data.
- Build crime data ingestion scripts for spatial data formats (GeoJSON, CSV).
- Integrate dynamic risk factor calculations into `GET /api/v1/safety-score`.
