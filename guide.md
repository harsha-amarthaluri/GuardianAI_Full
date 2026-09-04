# Guardian AI — Render.com Hosting & Deployment Guide

This guide provides step-by-step instructions to deploy the **Guardian AI FastAPI Backend** to [Render.com](https://render.com) and set up the **14-minute Keep-Alive Cron** to prevent free-tier instance sleeping.

---

## Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [Method 1: Deploying via Render Blueprint (`render.yaml`) — Recommended](#2-method-1-deploying-via-render-blueprint-renderyaml--recommended)
3. [Method 2: Manual Web Service Setup](#3-method-2-manual-web-service-setup)
4. [Database Setup (PostgreSQL)](#4-database-setup-postgresql)
5. [Setting Up 14-Minute Keep-Alive Cron](#5-setting-up-14-minute-keep-alive-cron)
6. [Updating the Android App API Base URL](#6-updating-the-android-app-api-base-url)
7. [Troubleshooting & Verification](#7-troubleshooting--verification)

---

## 1. Prerequisites

- A [Render.com Account](https://dashboard.render.com/register).
- A GitHub or GitLab repository containing your Guardian AI project.
- (Optional) PostgreSQL database instance (Render PostgreSQL, Supabase, Neon, or Railway).

---

## 2. Method 1: Deploying via Render Blueprint (`render.yaml`) — Recommended

The repository includes a pre-configured [`render.yaml`](render.yaml) file for automated setup.

### Step 1: Push Code to GitHub
Ensure all latest changes including `render.yaml` are pushed to your remote repository:
```bash
git add .
git commit -m "Configure Render hosting blueprint and keep-alive setup"
git push origin main
```

### Step 2: Create Blueprint on Render
1. Log in to [Render Dashboard](https://dashboard.render.com).
2. Click the **New +** button in the top right and select **Blueprint**.
3. Connect your GitHub/GitLab repository.
4. Render will automatically detect [`render.yaml`](render.yaml) and parse service configuration:
   - **Service Type**: Web Service (`guardian-ai-backend`)
   - **Environment**: Python
   - **Build Command**: `pip install -r backend/requirements.txt`
   - **Start Command**: `uvicorn backend.app.main:app --host 0.0.0.0 --port $PORT`
   - **Health Check Path**: `/health`

### Step 3: Configure Environment Variables
Before clicking deploy, configure the required environment variables:
- `ENVIRONMENT`: `production`
- `SECRET_KEY`: Set a strong random secret key (or let Render generate one).
- `DATABASE_URL`: Your PostgreSQL connection string (e.g. `postgresql://user:password@host:5432/guardian_db`).

### Step 4: Deploy
Click **Apply**. Render will build the container, install dependencies, and start the FastAPI service.

---

## 3. Method 2: Manual Web Service Setup (Step-by-Step UI Guide)

If you are creating the Web Service manually on Render via the Web Dashboard (**New + > Web Service**), fill in the fields exactly as shown below:

### Render Dashboard Configuration Form

| Render UI Field | Recommended Value | Description / Explanation |
| :--- | :--- | :--- |
| **Name** | `guardian-ai-backend` | Name of your web service on Render |
| **Language / Runtime** | `Python 3` | Python runtime environment |
| **Branch** | `main` | The Git branch to build and deploy |
| **Region** | `Ohio (US East)` *(or closest region)* | Server region |
| **Root Directory** | `backend` | **Important:** Set to `backend` so Render runs commands inside the backend folder |
| **Build Command** | `pip install -r requirements.txt` | Installs Python dependencies |
| **Start Command** | `alembic upgrade head && uvicorn app.main:app --host 0.0.0.0 --port $PORT` | Runs database migrations and starts FastAPI server |

---

### Advanced Configuration & Environment Variables

1. Scroll down to **Advanced Settings**:
   - **Health Check Path**: `/health`

2. Under **Environment Variables**, click **Add Environment Variable**:
   | Key | Value | Notes |
   | :--- | :--- | :--- |
   | `ENVIRONMENT` | `production` | Enables production mode |
   | `SECRET_KEY` | `<your-secret-key>` | JWT signing secret key |
   | `DATABASE_URL` | `postgresql://postgres.xdtvthdlxdpixhnsaeah:.%23Ccf*b%236y-Drzc@aws-0-ap-south-1.pooler.supabase.com:6543/postgres` | **Must use Supabase IPv4 Pooler URL** (Port `6543`) |

> [!NOTE]
> **Why IPv4 Pooler is required on Render**: Render free-tier instances do not support outbound IPv6. Direct Supabase URLs (`db.[ref].supabase.co:5432`) resolve to IPv6-only, causing `Network is unreachable` errors. The Supabase IPv4 Pooler connection string (`aws-0-[region].pooler.supabase.com:6543` with username `postgres.[project_ref]`) resolves to IPv4 and connects seamlessly.

3. Click **Create Web Service**. Render will pull your repository, install dependencies, run migrations on container startup, and launch your API.

---

## 4. Database Setup (PostgreSQL)

### Running Database Migrations (Alembic)
To automatically run Alembic database migrations during deployment, update your **Build Command** in Render to:

```bash
pip install -r backend/requirements.txt && alembic -c backend/alembic.ini upgrade head
```

This ensures your PostgreSQL tables and schemas are created/updated before the server launches.

---

## 5. Setting Up 14-Minute Keep-Alive Cron

Render free web services spin down after **15 minutes** of inactivity, causing cold-start delays on initial user requests. To keep your backend active 24/7, use one of the ping mechanisms below.

### Option A: GitHub Actions Automated Cron Ping (Automated & Cloud-Based)
The repository includes a GitHub Action workflow [`.github/workflows/keepalive.yml`](.github/workflows/keepalive.yml) configured to run every **14 minutes** (`*/14 * * * *`).

1. Copy your deployed Render URL (e.g. `https://guardian-ai-backend.onrender.com`).
2. Go to your GitHub repository **Settings > Secrets and variables > Actions**.
3. Click **New repository secret**:
   - **Name**: `RENDER_BACKEND_URL`
   - **Value**: `https://guardian-ai-backend.onrender.com/health`
4. GitHub Actions will now ping your backend every 14 minutes automatically.

### Option B: Local Python Keep-Alive Daemon
You can run the included Python script [`scripts/ping_keepalive.py`](scripts/ping_keepalive.py) continuously on any server or machine:

```powershell
python scripts/ping_keepalive.py --url https://guardian-ai-backend.onrender.com/health
```

To test a single ping once:
```powershell
python scripts/ping_keepalive.py --once --url https://guardian-ai-backend.onrender.com/health
```

---

## 6. Updating the Android App API Base URL

Once your backend is live on Render:

1. Open `app/src/main/java/com/guardianai/data/ApiClient.java` (or config file).
2. Change base URL from local emulator address to your live Render domain:
   ```java
   // Local Emulator address:
   // private static final String BASE_URL = "http://10.0.2.2:8000/api/v1/";
   
   // Production Render URL:
   private static final String BASE_URL = "https://guardian-ai-backend.onrender.com/api/v1/";
   ```

---

## 7. Troubleshooting & Verification

### Live Endpoint Checks
- **Health Check Endpoint**:
  ```bash
  curl -i https://<your-app>.onrender.com/health
  ```
  Expected Response: `200 OK` with `{"status":"healthy","database":"healthy",...}`

- **OpenAPI Interactive Documentation**:
  Navigate to `https://<your-app>.onrender.com/docs` in your browser.

### Checking Render Logs
- View live application logs in Render Dashboard under **Events** and **Logs**.
- If build fails, verify `backend/requirements.txt` contains all necessary dependencies (`fastapi`, `uvicorn`, `sqlalchemy`, `pydantic`, `alembic`, `psycopg2-binary` or `asyncpg`).
