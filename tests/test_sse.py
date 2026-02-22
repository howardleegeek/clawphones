from fastapi.testclient import TestClient
from app import app


def test_sse_stream_basic():
    client = TestClient(app)
    resp = client.get("/v1/conversations/abc123/chat/stream", stream=True)
    # Status and content type
    assert resp.status_code == 200
    assert "text/event-stream" in resp.headers.get("content-type", "")

    # Read first few lines from the stream
    lines = []
    for line in resp.iter_lines(decode_unicode=True):
        if line:
            lines.append(line)
        if len(lines) >= 2:
            break

    assert any("connecting" in l for l in lines)
    assert any("data: message" in l for l in lines)
