from marketing_console import MarketingConsoleService
from dashboard import load_dashboard


def test_dashboard_load_and_metrics():
    # reset in-memory store for deterministic test
    MarketingConsoleService._campaigns.clear()
    MarketingConsoleService._mobile_events.clear()
    c1 = MarketingConsoleService.create_campaign("A", 100.0)
    MarketingConsoleService.update_campaign(c1["id"], reach=100, conversions=5)
    c2 = MarketingConsoleService.create_campaign("B", 200.0)
    MarketingConsoleService.update_campaign(c2["id"], reach=50, conversions=2)
    dash = load_dashboard()
    assert "campaigns" in dash
    metrics = dash["metrics"]
    assert metrics["total_reach"] == 150
    assert metrics["total_conversions"] == 7
    assert abs(metrics["conversion_rate"] - (7 / 150)) < 1e-9
