from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
import redis
from pydantic import BaseModel
import asyncio
import json
import nats

app = FastAPI()

class CommunityData(BaseModel):
    communityId: str
    genW: int
    conW: int

@app.on_event("startup")
async def startup_event():
    app.state.redis = redis.Redis(host='redis', port=6379, decode_responses=True)
    app.state.nc = await nats.connect("nats://nats:4222")
    app.state.js = app.state.nc.jetstream()

    try:
        await app.state.js.add_stream(name="EC_STREAM", subjects=["ec.*.net"])
    except Exception:
        pass

@app.on_event("shutdown")
async def shutdown_event():
    app.state.redis.close()
    await app.state.nc.close()

@app.get('/communities/{community_id}')
async def get_latest(community_id:str):

    data = app.state.redis.get(f"ec:{community_id}:latest")

    if not data:
        raise HTTPException(status_code=404, detail="No data available")
    
    return json.loads(data)

@app.post("/communities/")
async def post_data(payload: CommunityData):
    data_dict = payload.dict()
    subject = f"ec.{data_dict['communityId']}.net"

    try:
        ack = await app.state.js.publish(subject, json.dumps(data_dict).encode())
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to publish to NATS: {e}")

    return {"status": "published", "subject": subject, "seq": ack.seq}

@app.get("/communities/{community_id}/stream")
async def stream_community(community_id: str):
    redis_client = app.state.redis
    async def event_generator():
        last_value = None
        while True:
            data = redis_client.get(f"ec:{community_id}:latest")
            if data:
                if data != last_value:
                    last_value = data
                    yield f"data: {data}\n\n"
            await asyncio.sleep(1) 
    return StreamingResponse(event_generator(), media_type="text/event-stream")
