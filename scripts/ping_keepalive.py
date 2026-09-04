#!/usr/bin/env python3
"""
Guardian AI — Keep-Alive Ping Script for Render Free Tier Hosting
Pings the backend /health endpoint every 14 minutes (840 seconds) to prevent service sleep.
"""

import sys
import os
import time
import json
import argparse
from datetime import datetime, timezone
import urllib.request
import urllib.error

DEFAULT_INTERVAL_SECONDS = 840  # 14 minutes
DEFAULT_URL = os.environ.get("RENDER_BACKEND_URL", "https://gai-backend-ylzf.onrender.com/health")

def ping(url: str) -> bool:
    """Send HTTP GET request to health endpoint and print status."""
    if not url.endswith("/health") and not url.endswith("/health/"):
        url = url.rstrip("/") + "/health"

    start_time = time.time()
    now_str = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")

    try:
        req = urllib.request.Request(
            url,
            headers={"User-Agent": "GuardianAI-KeepAlive-Cron/1.0"}
        )
        with urllib.request.urlopen(req, timeout=30) as response:
            latency_ms = round((time.time() - start_time) * 1000, 2)
            status_code = response.status
            body = response.read().decode('utf-8')
            
            try:
                data = json.loads(body)
                db_status = data.get("database", "unknown")
            except Exception:
                db_status = "N/A"

            print(f"[{now_str}] SUCCESS — Status: {status_code} | DB: {db_status} | Latency: {latency_ms}ms | URL: {url}")
            return True
    except urllib.error.HTTPError as e:
        latency_ms = round((time.time() - start_time) * 1000, 2)
        print(f"[{now_str}] HTTP ERROR — Code: {e.code} | Reason: {e.reason} | Latency: {latency_ms}ms | URL: {url}", file=sys.stderr)
        return False
    except Exception as e:
        latency_ms = round((time.time() - start_time) * 1000, 2)
        print(f"[{now_str}] FAILED — Error: {str(e)} | Latency: {latency_ms}ms | URL: {url}", file=sys.stderr)
        return False

def main():
    parser = argparse.ArgumentParser(description="Guardian AI Keep-Alive Ping Service for Render")
    parser.add_argument("--url", type=str, default=DEFAULT_URL, help="Target backend health URL (default: $RENDER_BACKEND_URL or http://localhost:8000/health)")
    parser.add_argument("--interval", type=int, default=DEFAULT_INTERVAL_SECONDS, help="Ping interval in seconds (default: 840s / 14 mins)")
    parser.add_argument("--once", action="store_true", help="Perform a single ping and exit")

    args = parser.parse_args()

    print("==================================================")
    print(" Guardian AI — 14-Minute Ping Keep-Alive Service")
    print(f" Target URL : {args.url}")
    print(f" Interval   : {args.interval}s ({args.interval // 60}m {args.interval % 60}s)")
    print(f" Mode       : {'Single Ping' if args.once else 'Continuous Daemon'}")
    print("==================================================")

    if args.once:
        success = ping(args.url)
        sys.exit(0 if success else 1)

    try:
        while True:
            ping(args.url)
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nKeep-Alive service stopped by user.")
        sys.exit(0)

if __name__ == "__main__":
    main()
