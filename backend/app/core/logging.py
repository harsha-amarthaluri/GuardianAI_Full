import logging
import re
from app.core.config import settings

class SensitiveDataFilter(logging.Filter):
    """
    Filter out sensitive key patterns such as passwords, hashes, and tokens from log messages.
    """
    SENSITIVE_PATTERNS = [
        r'(password[\'\"]?\s*[:=]\s*[\'\"]?)[^\'\"\s,]+',
        r'(hashed_password[\'\"]?\s*[:=]\s*[\'\"]?)[^\'\"\s,]+',
        r'(access_token[\'\"]?\s*[:=]\s*[\'\"]?)[^\'\"\s,]+',
        r'(Bearer\s+)[A-Za-z0-9\-\._~\+\/]+=*',
    ]

    def filter(self, record: logging.LogRecord) -> bool:
        if isinstance(record.msg, str):
            for pattern in self.SENSITIVE_PATTERNS:
                record.msg = re.sub(pattern, r'\1***REDACTED***', record.msg)
        return True

def setup_logging():
    log_level = logging.DEBUG if settings.DEBUG else logging.INFO
    logging.basicConfig(
        level=log_level,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
    )
    logger = logging.getLogger("guardian_ai")
    sensitive_filter = SensitiveDataFilter()
    logger.addFilter(sensitive_filter)
    return logger

logger = setup_logging()
