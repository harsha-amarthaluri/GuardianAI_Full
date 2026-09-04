# Guardian AI — Security & Privacy Architecture Specification

## 1. Threat Model & Security Principles

As a proactive personal safety platform, Guardian AI handles sensitive user location telemetry, emergency contact networks, and real-time emergency events. Security and data privacy are core design constraints.

### Key Threats & Mitigations

| Threat Vector | Mitigation Strategy |
| :--- | :--- |
| **On-Device Token Theft** | Android JWT tokens stored using Keystore-backed `EncryptedSharedPreferences` (`TokenManager`). Passwords are never stored on device. |
| **Session Hijacking / Expiration** | OkHttp response interceptor catches HTTP 401 Unauthorized globally (`SessionManager`), automatically clearing tokens and routing to Login. |
| **Location Telemetry Interception** | Mandatory TLS 1.3 / HTTPS encryption in transit; encrypted storage at rest. |
| **Unauthorized Resource Access** | All user-owned endpoints require Bearer JWT. User identity is derived strictly from JWT payload (`sub`). Clients cannot supply arbitrary `user_id`. |
| **Credential Harvesting** | Passwords hashed using `bcrypt` (work factor 12); raw passwords and hashes excluded from log files and API responses. |
| **Secret Exposure** | No hardcoded keys or database passwords in code repositories; environment variable injection (`.env`). |
| **Cross-Origin Attacks** | Configurable CORS middleware (`BACKEND_CORS_ORIGINS`). |

---

## 2. Authentication & Authorization Framework

- **Protocol**: OAuth2 HTTP Bearer JSON Web Tokens (JWT).
- **Token Storage**: Keystore-backed `EncryptedSharedPreferences` (`TokenManager`).
- **Token Expiry & Cleanup**: Centralized `SessionManager` clears local token on 401 response and redirects to Login.
- **Authorization Enforcement**:
  - All protected endpoints depend on `get_current_user`.
  - Repositories query resources strictly matching `(resource_id, user_id)`.
  - Attempts to access another user's guardian, location, or SOS incident return `404 Not Found`.

---

## 3. Data Protection & Sensitive Log Filtering

1. **Logging Filter**: `SensitiveDataFilter` automatically redacts passwords, hashes, and Bearer tokens matching regex patterns.
2. **Minimization**: Only essential location telemetry (latitude, longitude, accuracy, timestamp) is accepted.

---

## 4. Input Validation & Parameter Safety

- **Android Client Validation**: `ValidationUtils` verifies email format, password min length 8, password matching, and coordinate boundaries (`latitude`: -90.0 to 90.0, `longitude`: -180.0 to 180.0).
- **FastAPI Validation**: Pydantic v2 schemas enforce server-side validation.
- **SQL Injection Prevention**: Parameterized queries using SQLAlchemy ORM.
