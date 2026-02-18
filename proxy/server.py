from fastapi import FastAPI, APIRouter, Depends, HTTPException, Header
from pydantic import BaseModel
import logging

app = FastAPI()
router = APIRouter()
logger = logging.getLogger("token_disable")

# In-memory token status store. If a token is not present, it is considered enabled by default.
token_status = {}

def admin_required(x_admin_token: str = Header(None, alias="X-Admin-Token")):
    # Simple admin gate. In real deployments this would be OAuth2/JWT etc.
    if x_admin_token != "admin-secret":
        raise HTTPException(status_code=403, detail="Admin privileges required")

class TokenStatus(BaseModel):
    enabled: bool

@router.get("/admin/tokens/{token}/status")
def get_token_status(token: str, task_id: str = Header(None, alias="X-Task-Id"), _=Depends(admin_required)):
    """Return the enabled/disabled status for a token."""
    status = token_status.get(token, True)
    logger.info("Token status retrieved", extra={"task_id": task_id or "unknown"})
    return {"token": token, "enabled": status}

@router.patch("/admin/tokens/{token}/status")
def set_token_status(token: str, status: TokenStatus, task_id: str = Header(None, alias="X-Task-Id"), _=Depends(admin_required)):
    """Enable or disable a token."""
    token_status[token] = status.enabled
    logger.info("Token status updated", extra={"task_id": task_id or "unknown"})
    return {"token": token, "enabled": status.enabled}

app.include_router(router)
