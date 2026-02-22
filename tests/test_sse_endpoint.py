from fastapi.testclient import TestClient
from proxy.server import app

client = TestClient(app)


def test_sse_stream_endpoint():
    resp = client.get("/v1/conversations/123/chat/stream", stream=True)
    assert resp.status_code == 200
    assert "text/event-stream" in resp.headers.get("content-type", "")
    text = resp.text
    assert "data: Conversation 123 - event 1" in text
    assert "data: Conversation 123 - event 2" in text
    assert "data: Conversation 123 - end" in text


def test_non_stream_endpoint():
    resp = client.get("/v1/conversations/999/chat")
    assert resp.status_code == 200
    data = resp.json()
    assert data["conversation_id"] == "999"
    assert "messages" in data
