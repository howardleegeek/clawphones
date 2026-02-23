from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import time
import json
import logging

app = FastAPI()

# Basic structured logger including task_id for traceability per spec
LOG = logging.getLogger("sse_proxy")
LOG.setLevel(logging.INFO)
if not LOG.handlers:
    ch = logging.StreamHandler()
    ch.setFormatter(logging.Formatter("[%(asctime)s] [task_id=%(task_id)s] %(levelname)s: %(message)s"))
    LOG.addHandler(ch)

def _log(task_id: str, message: str):
    LOG.info(message, extra={"task_id": task_id})

def _event_stream(convo_id: str):
    # Simple SSE stream emitting 3 chunks
    _log("S01-012", f"starting SSE stream for convo {convo_id}")
    for i in range(3):
        payload = {"convo_id": convo_id, "chunk": i}
        yield f"data: {json.dumps(payload)}\n\n"
        time.sleep(0.01)
    yield "data: {\"end\": true}\n\n"
    _log("S01-012", f"ended SSE stream for convo {convo_id}")

@app.get("/v1/conversations/{id}/chat/stream")
def chat_stream(id: str):
    # StreamingResponse with SSE content type
    return StreamingResponse(_event_stream(id), media_type="text/event-stream")

# Non-streaming endpoint to ensure backward compatibility for other clients
@app.get("/health")
def health():
    return {"status": "ok"}
