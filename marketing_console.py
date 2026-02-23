import time
import logging
from typing import Any, Dict, Optional, List

"""
Marketing Console Service (CM) - in-memory prototype for tests.
Exposes CRUD operations and real-time-like stats with lightweight
integration events to a hypothetical mobile client.
"""

logger = logging.getLogger("MarketingConsoleService")


class MarketingConsoleService:
    _campaigns: Dict[int, Dict[str, Any]] = {}
    _id_counter: int = 1
    _mobile_events: List[Dict[str, Any]] = []

    @classmethod
    def _emit_mobile_event(cls, event_type: str, payload: Dict[str, Any], task_id: Optional[str] = None) -> None:
        event = {"type": event_type, "payload": payload, "task_id": task_id}
        cls._mobile_events.append(event)
        logger.info("Push to mobile: %s", event, extra={"task_id": task_id})

    @classmethod
    def get_mobile_events(cls) -> List[Dict[str, Any]]:
        return list(cls._mobile_events)

    @classmethod
    def _new_id(cls) -> int:
        cid = cls._id_counter
        cls._id_counter += 1
        return cid

    @classmethod
    def create_campaign(cls, name: str, budget: float, target: Optional[Any] = None, task_id: Optional[str] = None) -> Dict[str, Any]:
        cid = cls._new_id()
        campaign = {
            "id": cid,
            "name": name,
            "budget": float(budget),
            "target": target,
            "status": "active",
            "created_at": time.time(),
            "reach": 0,
            "conversions": 0,
        }
        cls._campaigns[cid] = campaign
        logger.info("Created campaign %s", cid, extra={"task_id": task_id})
        cls._emit_mobile_event("campaign_created", {"id": cid}, task_id)
        return dict(campaign)

    @classmethod
    def get_campaign(cls, cid: int) -> Dict[str, Any]:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        return dict(cls._campaigns[cid])

    @classmethod
    def update_campaign(cls, cid: int, **kwargs) -> Dict[str, Any]:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        cls._campaigns[cid].update(kwargs)
        return dict(cls._campaigns[cid])

    @classmethod
    def delete_campaign(cls, cid: int) -> None:
        if cid in cls._campaigns:
            del cls._campaigns[cid]
        else:
            raise ValueError(f"Campaign {cid} not found")

    @classmethod
    def pause_campaign(cls, cid: int, task_id: Optional[str] = None) -> None:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        cls._campaigns[cid]["status"] = "paused"
        cls._emit_mobile_event("campaign_paused", {"id": cid}, task_id)

    @classmethod
    def resume_campaign(cls, cid: int, task_id: Optional[str] = None) -> None:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        cls._campaigns[cid]["status"] = "active"
        cls._emit_mobile_event("campaign_resumed", {"id": cid}, task_id)

    @classmethod
    def adjust_budget(cls, cid: int, new_budget: float, task_id: Optional[str] = None) -> None:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        cls._campaigns[cid]["budget"] = float(new_budget)
        cls._emit_mobile_event("campaign_budget_updated", {"id": cid, "budget": new_budget}, task_id)

    @classmethod
    def get_realtime_stats(cls, cid: int) -> Dict[str, Any]:
        if cid not in cls._campaigns:
            raise ValueError(f"Campaign {cid} not found")
        c = cls._campaigns[cid]
        reach = max(0, int(c.get("reach", 0)))
        conversions = max(0, int(c.get("conversions", 0)))
        rate = conversions / reach if reach > 0 else 0.0
        return {
            "id": cid,
            "status": c.get("status"),
            "reach": reach,
            "conversions": conversions,
            "conversion_rate": rate,
            "budget": c.get("budget"),
        }

    @classmethod
    def get_all_campaigns(cls) -> List[Dict[str, Any]]:
        return [dict(c) for c in cls._campaigns.values()]
