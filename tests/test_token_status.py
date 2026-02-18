import pathlib
import importlib.util
import pytest

from fastapi.testclient import TestClient


@pytest.fixture(scope="session")
def app():
    # Load the FastAPI app from the standalone module without requiring a package import
    repo_root = pathlib.Path(__file__).resolve().parents[1]
    server_path = repo_root / "proxy" / "server.py"
    spec = importlib.util.spec_from_file_location("proxy_server", str(server_path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)  # type: ignore
    return module.app  # type: ignore


@pytest.fixture
def client(app):
    return TestClient(app)


def test_admin_required_missing(client):
    resp = client.get("/admin/tokens/sample/status")
    assert resp.status_code == 403


def test_admin_required_wrong(client):
    resp = client.get("/admin/tokens/sample/status", headers={"X-Admin-Token": "wrong"})
    assert resp.status_code == 403


def test_get_default_status_enabled(client):
    resp = client.get("/admin/tokens/sample/status", headers={"X-Admin-Token": "admin-secret"})
    assert resp.status_code == 200
    data = resp.json()
    assert data["token"] == "sample"
    assert data["enabled"] is True


def test_disable_token(client):
    resp = client.patch(
        "/admin/tokens/sample/status",
        json={"enabled": False},
        headers={"X-Admin-Token": "admin-secret"},
    )
    assert resp.status_code == 200
    assert resp.json()["enabled"] is False

    resp2 = client.get("/admin/tokens/sample/status", headers={"X-Admin-Token": "admin-secret"})
    assert resp2.status_code == 200
    assert resp2.json()["enabled"] is False


def test_enable_token(client):
    resp = client.patch(
        "/admin/tokens/sample/status",
        json={"enabled": True},
        headers={"X-Admin-Token": "admin-secret"},
    )
    assert resp.status_code == 200
    assert resp.json()["enabled"] is True

    resp2 = client.get("/admin/tokens/sample/status", headers={"X-Admin-Token": "admin-secret"})
    assert resp2.status_code == 200
    assert resp2.json()["enabled"] is True
