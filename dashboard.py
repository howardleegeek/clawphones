from typing import Dict, Any, Optional
from marketing_console import MarketingConsoleService


def load_dashboard(task_id: Optional[str] = None) -> Dict[str, Any]:
    """Load dashboard data aggregating campaigns and metrics.
    This is a lightweight in-memory representation for tests.
    """
    campaigns = MarketingConsoleService.get_all_campaigns()
    total_reach = sum((c.get("reach") or 0) for c in campaigns)
    total_conversions = sum((c.get("conversions") or 0) for c in campaigns)
    overall_rate = total_conversions / total_reach if total_reach > 0 else 0.0
    return {
        "campaigns": campaigns,
        "metrics": {
            "total_reach": total_reach,
            "total_conversions": total_conversions,
            "conversion_rate": overall_rate,
        },
    }
