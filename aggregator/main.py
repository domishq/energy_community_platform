import asyncio
import json
import redis
import nats
from nats.js.api import KeyValueConfig

async def main():
    # Connect to Redis
    r = redis.Redis(host="redis", port=6379, decode_responses=True)

    # Connect to NATS
    nc = await nats.connect("nats://nats:4222")

    # JetStream context
    js = nc.jetstream()

    # Create or open KV bucket
    try:
        await js.create_key_value(KeyValueConfig(bucket="ec_latest"))
    except Exception:
        # bucket may already exist
        pass
    
    # Await key value
    kv = await js.key_value("ec_latest")

    # Ensure stream exists for "ec.*.net"
    try:
        await js.add_stream(name="EC_STREAM", subjects=["ec.*.net"])
    except Exception:
        # stream may already exist
        pass

    async def handler(msg):
        data = json.loads(msg.data.decode())
        cid = data["communityId"]
        genW = data["genW"]
        conW = data["conW"]

        store_data = {
            "genW": genW,
            "conW": conW
        }

        # Write to Redis
        r.set(f"ec:{cid}:latest", json.dumps(store_data), ex=300)  # expires in 5m

        # Write to NATS KV
        await kv.put(f"community/{cid}", json.dumps(store_data).encode())

        print(f"[Aggregator] Updated {cid} -> {store_data}")

    # Subscribe to all net values
    await js.subscribe("ec.*.net", cb=handler, durable="agg-worker")

    print("Aggregator running...")
    while True:
        await asyncio.sleep(1)

if __name__ == "__main__":
    asyncio.run(main())
