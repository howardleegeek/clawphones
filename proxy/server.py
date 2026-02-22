import time
import logging
from typing import Generator

from fastapi import FastAPI
from fastapi.responses import StreamingResponse, JSONResponse

app = FastAPI()
logger = logging.getLogger("proxy.server")


def sse_event_generator(convo_id: str) -> Generator[bytes, None, None]:
    # Simple SSE generator: yields a few events then ends
    events = [
        f"data: Conversation {convo_id} - event 1",
        f"data: Conversation {convo_id} - event 2",
        f"data: Conversation {convo_id} - end",
    ]
    for ev in events:
        yield (ev + "\n\n").encode("utf-8")
        time.sleep(0.01)


@app.get("/v1/conversations/{convo_id}/chat/stream")
def chat_stream(convo_id: str):
    logger.info(f"Starting SSE stream for convo {convo_id}")
    return StreamingResponse(sse_event_generator(convo_id), media_type="text/event-stream")


@app.get("/v1/conversations/{convo_id}/chat")
def chat(convo_id: str):
    # Non-streaming fallback endpoint for backward compatibility
    data = {
        "conversation_id": convo_id,
        "messages": [
            {"role": "system", "content": "Hello"},
            {"role": "user", "content": "Hi"},
        ],
    }
    return JSONResponse(content=data)
