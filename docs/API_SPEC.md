# Guardian AI — REST API Specification

## Base URL
Default Local Server: `http://localhost:8000/api/v1`  
Android Emulator Loopback: `http://10.0.2.2:8000/api/v1`

---

## 1. Authentication Endpoints

### `POST /auth/register`
Creates a new user account.

#### Request Body
```json
{
  "full_name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phone_number": "+1234567890",
  "password": "SecurePassword123!"
}
```

#### Response (`201 Created`)
```json
{
  "id": "u_9a8b7c6d5e4f3a2b",
  "full_name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phone_number": "+1234567890",
  "role": "USER",
  "is_active": true,
  "is_verified": false,
  "created_at": "2026-08-25T10:00:00Z"
}
```

---

### `POST /auth/login`
Authenticates credentials and returns a Bearer access token.

#### Request Body
```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePassword123!"
}
```

#### Response (`200 OK`)
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "expires_in": 86400
}
```

---

## 2. Location Telemetry Endpoints

### `POST /locations`
Submits single client GPS location point for the authenticated user.

#### Headers
`Authorization: Bearer <JWT>`

#### Request Body
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "accuracy": 5.0,
  "timestamp": "2026-08-25T10:15:00Z"
}
```

#### Response (`201 Created`)
```json
{
  "id": 101,
  "user_id": "u_9a8b7c6d5e4f3a2b",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "accuracy": 5.0,
  "timestamp": "2026-08-25T10:15:00Z"
}
```

---

### `POST /locations/batch`
Submits batch of queued offline GPS location points for the authenticated user with authoritative server-side deduplication.

#### Headers
`Authorization: Bearer <JWT>`

#### Request Body
```json
{
  "locations": [
    {
      "latitude": 37.7749,
      "longitude": -122.4194,
      "accuracy": 5.0,
      "timestamp": "2026-08-25T10:15:00Z"
    },
    {
      "latitude": 37.7800,
      "longitude": -122.4100,
      "accuracy": 4.2,
      "timestamp": "2026-08-25T10:15:30Z"
    }
  ]
}
```

#### Response (`201 Created`)
```json
{
  "processed_count": 2,
  "ignored_duplicates_count": 0,
  "items": [
    {
      "id": 102,
      "user_id": "u_9a8b7c6d5e4f3a2b",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "accuracy": 5.0,
      "timestamp": "2026-08-25T10:15:00Z"
    },
    {
      "id": 103,
      "user_id": "u_9a8b7c6d5e4f3a2b",
      "latitude": 37.7800,
      "longitude": -122.4100,
      "accuracy": 4.2,
      "timestamp": "2026-08-25T10:15:30Z"
    }
  ]
}
```

---

## 3. Safety Score Endpoints

### `GET /safety-score?latitude={lat}&longitude={lon}`
Returns situational safety score evaluation for specified coordinates.

#### Headers
`Authorization: Bearer <JWT>`

#### Response (`200 OK`)
```json
{
  "score": 85.0,
  "category": "LOW",
  "location": {
    "latitude": 37.7749,
    "longitude": -122.4194
  },
  "factors": [],
  "disclaimer": "Safety score is an estimate based on contextual indicators. It does not guarantee safety."
}
```

---

## 4. Emergency SOS Endpoints

### `POST /sos`
Triggers an emergency SOS incident.

#### Headers
`Authorization: Bearer <JWT>`

#### Request Body
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "trigger_type": "MANUAL"
}
```

#### Response (`201 Created`)
```json
{
  "id": "sos_1a2b3c4d",
  "user_id": "u_9a8b7c6d5e4f3a2b",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "trigger_type": "MANUAL",
  "risk_score": null,
  "status": "ALERTING",
  "created_at": "2026-08-25T10:20:00Z",
  "updated_at": "2026-08-25T10:20:00Z",
  "resolved_at": null
}
```

---

### `GET /sos?skip=0&limit=20`
Lists user's past emergency incidents.

#### Headers
`Authorization: Bearer <JWT>`

#### Response (`200 OK`)
```json
{
  "items": [
    {
      "id": "sos_1a2b3c4d",
      "user_id": "u_9a8b7c6d5e4f3a2b",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "trigger_type": "MANUAL",
      "risk_score": null,
      "status": "ALERTING",
      "created_at": "2026-08-25T10:20:00Z",
      "updated_at": "2026-08-25T10:20:00Z",
      "resolved_at": null
    }
  ],
  "total": 1,
  "skip": 0,
  "limit": 20
}
```
