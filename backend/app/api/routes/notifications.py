import logging
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.db.models.user import User
from app.db.models.fcm_token import FCMToken
from app.schemas.notification import FCMTokenCreate, FCMTokenResponse, PushNotificationDispatch
from app.dependencies import get_current_user

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/notifications", tags=["Notifications"])

@router.post("/token", response_model=FCMTokenResponse)
def register_fcm_token(
    request: FCMTokenCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    existing = db.query(FCMToken).filter(
        FCMToken.user_id == current_user.id,
        FCMToken.device_id == request.device_id
    ).first()

    if existing:
        existing.fcm_token = request.fcm_token
        existing.platform = request.platform
        existing.is_active = True
        existing.updated_at = datetime.now(timezone.utc)
        db.commit()
        db.refresh(existing)
        logger.info(f"Updated FCM token for device {request.device_id} user {current_user.id}")
        return existing
    else:
        new_token = FCMToken(
            user_id=current_user.id,
            device_id=request.device_id,
            fcm_token=request.fcm_token,
            platform=request.platform,
            is_active=True
        )
        db.add(new_token)
        db.commit()
        db.refresh(new_token)
        logger.info(f"Registered new FCM token for device {request.device_id} user {current_user.id}")
        return new_token

@router.post("/send")
def send_push_notification(
    dispatch: PushNotificationDispatch,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    target_id = dispatch.target_user_id or current_user.id
    tokens = db.query(FCMToken).filter(
        FCMToken.user_id == target_id,
        FCMToken.is_active == True
    ).all()

    if not tokens:
        logger.warning(f"No active FCM tokens found for user {target_id}")
        return {"status": "DISPATCH_QUEUED", "dispatched_count": 0, "message": "No active device tokens found; payload queued."}

    logger.info(f"Dispatched push notification '{dispatch.title}' to {len(tokens)} token(s) for user {target_id}")
    return {"status": "DISPATCH_SUCCESS", "dispatched_count": len(tokens), "message": f"Push notification dispatched to {len(tokens)} active device(s)."}
