from proxy.server import app
from fastapi.testclient import TestClient


def test_sse_stream_endpoint():
    client = TestClient(app)
    resp = client.get("/v1/conversations/abc/chat/stream")
    assert resp.status_code == 200
    assert resp.headers.get("content-type", "").startswith("text/event-stream")
    text = resp.text
    assert 'convo_id' in text
    assert '"end"' in text or 'end' in text


def test_health_endpoint_unchanged():
    client = TestClient(app)
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "ok"}
