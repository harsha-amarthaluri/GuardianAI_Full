import os
import json
import urllib.request
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, status
from typing import Dict, Any

from app.db.models.user import User
from app.dependencies.auth import get_current_user
from app.schemas.feature_schemas import ChatMessageRequest, ChatMessageResponse

router = APIRouter()

EMERGENCY_KEYWORDS = [
    "attack", "kidnap", "in danger", "sos help", "help me", "stalker", "bleeding",
    "weapon", "followed", "hostage", "robbery", "assault"
]

@router.post(
    "/message",
    response_model=ChatMessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Send message to AI Support Assistant",
    description="Processes user safety questions or distress messages via OpenRouter AI or fallback safety reasoning."
)
def send_chat_message(
    request: ChatMessageRequest,
    current_user: User = Depends(get_current_user)
):
    user_text = request.message.strip()
    lower_text = user_text.lower()
    
    # Check for emergency triggers
    is_emergency = any(kw in lower_text for kw in EMERGENCY_KEYWORDS)
    suggested_actions = []
    
    if is_emergency:
        suggested_actions.append("TRIGGER_EMERGENCY_SOS")
        suggested_actions.append("CALL_EMERGENCY_SERVICES")
        suggested_actions.append("NOTIFY_GUARDIANS")

    # OpenRouter API call if key is provided in environment
    openrouter_api_key = os.getenv("OPENROUTER_API_KEY")
    reply_text = None

    if openrouter_api_key:
        try:
            payload = {
                "model": "meta-llama/llama-3-8b-instruct:free",
                "messages": [
                    {
                        "role": "system",
                        "content": "You are Guardian AI, a personal safety assistant. Provide clear, concise, calm, and actionable safety guidance. Prioritize immediate user physical safety."
                    },
                    {"role": "user", "content": user_text}
                ]
            }
            req = urllib.request.Request(
                "https://openrouter.ai/api/v1/chat/completions",
                data=json.dumps(payload).encode("utf-8"),
                headers={
                    "Authorization": f"Bearer {openrouter_api_key}",
                    "Content-Type": "application/json"
                },
                method="POST"
            )
            with urllib.request.urlopen(req, timeout=5) as response:
                res_data = json.loads(response.read().decode("utf-8"))
                reply_text = res_data["choices"][0]["message"]["content"]
        except Exception:
            reply_text = None

    # Structured Safety Reasoning Fallback
    if not reply_text:
        if is_emergency:
            reply_text = "⚠️ EMERGENCY DETECTED: If you are in immediate danger, please press the red EMERGENCY SOS button on your dashboard immediately to notify your trusted guardians and send your live GPS coordinates!"
        elif "sos" in lower_text or "emergency" in lower_text:
            reply_text = "Emergency SOS notifies your guardians instantly with your live GPS location, audio recording, and battery status. You can activate it manually or via rapid shake motion."
        elif "score" in lower_text or "risk" in lower_text:
            reply_text = "Your Safety Score evaluates active crime density, late-night temporal factors, weather hazards, and movement patterns. Check your Home dashboard for live updates."
        elif "guardian" in lower_text:
            reply_text = "You can add and manage trusted guardians in the Guardians tab. Guardians receive priority push notifications during SOS alerts."
        elif "route" in lower_text or "navigation" in lower_text:
            reply_text = "Use the Safe Route feature on the Map tab to evaluate alternative corridors. Route A (Primary) and Route B (Bypass) score threat density along your path."
        else:
            reply_text = "Guardian AI is actively monitoring your situational safety. I am here to help with safety questions, check-in timers, and emergency setup."

    return ChatMessageResponse(
        reply=reply_text,
        is_emergency_detected=is_emergency,
        suggested_actions=suggested_actions,
        timestamp=datetime.now(timezone.utc).isoformat()
    )
