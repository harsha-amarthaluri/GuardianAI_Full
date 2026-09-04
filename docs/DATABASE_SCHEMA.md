# Guardian AI — Database Schema Specification (SQLAlchemy / Alembic)

## 1. Relational Database Strategy

Guardian AI utilizes **SQLAlchemy 2.0 ORM** with **Alembic** migrations.

- **Storage Engine**: Supabase PostgreSQL for persistent cloud storage and SQLite for fast local unit testing environments.
- **Primary Key Strategy**: UUID strings (`VARCHAR(36)`) for core entities (`users`, `guardians`, `sos_incidents`) and Auto-incrementing Integers for high-frequency logs (`locations`, `safety_scores`, `crime_data`).

---

## 2. Table Specifications

### 2.1 `users`
Stores user profile information, authentication credentials, and system roles.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `PRIMARY KEY` | Unique User UUID |
| `email` | `VARCHAR(255)` | `UNIQUE, NOT NULL, INDEX` | User Email |
| `phone_number` | `VARCHAR(30)` | `NULLABLE` | Phone Number |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Full Name |
| `hashed_password` | `VARCHAR(255)` | `NOT NULL` | bcrypt Hashed Password |
| `role` | `VARCHAR(20)` | `DEFAULT 'USER'` | User Role |
| `is_active` | `BOOLEAN` | `DEFAULT TRUE` | Account Active Status |
| `is_verified` | `BOOLEAN` | `DEFAULT FALSE` | Verification Status |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Registration Time |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Last Update Time |

---

### 2.2 `guardians`
Stores trusted contacts linked to users for emergency notifications.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `PRIMARY KEY` | Guardian Record UUID |
| `user_id` | `VARCHAR(36)` | `FOREIGN KEY (users.id) ON DELETE CASCADE, INDEX` | Owner User ID |
| `name` | `VARCHAR(100)` | `NOT NULL` | Guardian Name |
| `phone` | `VARCHAR(30)` | `NOT NULL` | Guardian Phone |
| `email` | `VARCHAR(255)` | `NULLABLE` | Guardian Email |
| `relationship` | `VARCHAR(50)` | `NOT NULL` | Relationship |
| `notification_enabled` | `BOOLEAN` | `DEFAULT TRUE` | Alert Switch |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Creation Time |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Last Update Time |

---

### 2.3 `locations`
Stores historical client GPS telemetry points for emergency context.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Telemetry ID |
| `user_id` | `VARCHAR(36)` | `FOREIGN KEY (users.id) ON DELETE CASCADE, INDEX` | Owner User ID |
| `latitude` | `FLOAT` | `NOT NULL` | Latitude (-90 to 90) |
| `longitude` | `FLOAT` | `NOT NULL` | Longitude (-180 to 180) |
| `accuracy` | `FLOAT` | `NULLABLE` | Horizontal Accuracy (meters) |
| `timestamp` | `TIMESTAMPTZ` | `NOT NULL` | Telemetry Recorded Timestamp |

---

### 2.4 `safety_scores`
Stores calculated spatial safety scores.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Score Record ID |
| `user_id` | `VARCHAR(36)` | `FOREIGN KEY (users.id) ON DELETE CASCADE, NULLABLE` | Optional User ID |
| `latitude` | `FLOAT` | `NOT NULL` | Latitude |
| `longitude` | `FLOAT` | `NOT NULL` | Longitude |
| `score` | `FLOAT` | `NOT NULL` | Calculated Score (0-100) |
| `risk_category` | `VARCHAR(20)` | `NOT NULL` | LOW, MODERATE, HIGH, CRITICAL |
| `contributing_factors` | `JSON` | `NULLABLE` | JSON List of Factors |
| `timestamp` | `TIMESTAMPTZ` | `NOT NULL` | Evaluation Timestamp |

---

### 2.5 `sos_incidents`
Stores emergency SOS events.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `PRIMARY KEY` | Incident UUID |
| `user_id` | `VARCHAR(36)` | `FOREIGN KEY (users.id) ON DELETE CASCADE, INDEX` | Owner User ID |
| `latitude` | `FLOAT` | `NOT NULL` | Incident Latitude |
| `longitude` | `FLOAT` | `NOT NULL` | Incident Longitude |
| `trigger_type` | `VARCHAR(30)` | `NOT NULL` | MANUAL, SHAKE, STILLNESS, VOICE, SYSTEM |
| `risk_score` | `FLOAT` | `NULLABLE` | Risk Coefficient |
| `status` | `VARCHAR(20)` | `DEFAULT 'DETECTED'` | DETECTED, ALERTING, ACKNOWLEDGED, RESOLVED, FALSE_ALARM, FAILED |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Trigger Timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Status Update Timestamp |
| `resolved_at` | `TIMESTAMPTZ` | `NULLABLE` | Resolution Timestamp |

---

### 2.6 `crime_data`
Stores aggregated historical and real-time public crime incident records.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Crime Record ID |
| `location_name` | `VARCHAR(100)` | `NULLABLE` | Geographic Region Name |
| `latitude` | `FLOAT` | `NOT NULL` | Crime Latitude |
| `longitude` | `FLOAT` | `NOT NULL` | Crime Longitude |
| `crime_type` | `VARCHAR(50)` | `NOT NULL` | Category |
| `occurred_at` | `TIMESTAMPTZ` | `NULLABLE` | Timestamp of Occurrence |
| `severity` | `FLOAT` | `DEFAULT 5.0` | Severity Weight |
| `source` | `VARCHAR(50)` | `NULLABLE` | Data Source |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Ingestion Timestamp |
