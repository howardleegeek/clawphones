from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import asyncio

app = FastAPI(title="SSE Demo API")


@app.get("/v1/conversations/{conversation_id}/chat/stream")
async def chat_stream(conversation_id: str):
    async def event_generator():
        # Initial connection event
        yield f"data: connecting to conversation {conversation_id}\n\n"
        # Stream a few sample messages
        for i in range(3):
            await asyncio.sleep(0.05)
            yield f"data: message {i+1} for conversation {conversation_id}\n\n"
        # End of stream
        yield "data: end\n\n"

    return StreamingResponse(event_generator(), media_type="text/event-stream")
