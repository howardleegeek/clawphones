import time
from marketing_console import MarketingConsoleService


def test_create_campaign():
    c = MarketingConsoleService.create_campaign("Spring Sale", 1000.0, target={"channel": "mobile"}, task_id="tid-1")
    assert isinstance(c.get("id"), int)
    assert c["name"] == "Spring Sale"
    assert c["budget"] == 1000.0
    assert c["status"] == "active"


def test_pause_and_events():
    c = MarketingConsoleService.create_campaign("PauseTest", 500.0)
    cid = c["id"]
    MarketingConsoleService.pause_campaign(cid, task_id="tid-2")
    updated = MarketingConsoleService.get_campaign(cid)
    assert updated["status"] == "paused"
    events = MarketingConsoleService.get_mobile_events()
    assert any(e["type"] == "campaign_paused" and e["payload"].get("id") == cid for e in events)


def test_resume_and_budget():
    c = MarketingConsoleService.create_campaign("BudgetTest", 200.0)
    cid = c["id"]
    MarketingConsoleService.adjust_budget(cid, 300.0, task_id="tid-3")
    assert MarketingConsoleService.get_campaign(cid)["budget"] == 300.0
    MarketingConsoleService.resume_campaign(cid, task_id="tid-4")
    assert MarketingConsoleService.get_campaign(cid)["status"] == "active"


def test_realtime_stats():
    c = MarketingConsoleService.create_campaign("StatsTest", 100.0)
    cid = c["id"]
    MarketingConsoleService.update_campaign(cid, reach=150, conversions=15)
    stats = MarketingConsoleService.get_realtime_stats(cid)
    assert stats["conversion_rate"] == 15 / 150
