from fastapi.testclient import TestClient
from proxy.server import app

client = TestClient(app)

ADMIN_HEADER = {"X-Admin-Token": "admin-secret"}

def test_admin_required_missing_header_get():
    resp = client.get("/admin/tokens/abc/status")
    assert resp.status_code == 403

def test_admin_required_missing_header_patch():
    resp = client.patch("/admin/tokens/abc/status", json={"enabled": False})
    assert resp.status_code == 403

def test_get_default_status_with_admin():
    resp = client.get("/admin/tokens/xyz/status", headers=ADMIN_HEADER)
    assert resp.status_code == 200
    assert resp.json() == {"token": "xyz", "enabled": True}

def test_disable_and_retrieve_status():
    # Disable token
    resp = client.patch("/admin/tokens/abc/status", json={"enabled": False}, headers=ADMIN_HEADER)
    assert resp.status_code == 200
    assert resp.json() == {"token": "abc", "enabled": False}

    # Retrieve to confirm
    resp2 = client.get("/admin/tokens/abc/status", headers=ADMIN_HEADER)
    assert resp2.status_code == 200
    assert resp2.json() == {"token": "abc", "enabled": False}
