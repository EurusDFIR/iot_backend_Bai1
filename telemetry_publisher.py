import time
import json
import random
from paho.mqtt import client as mqtt

BROKER = "localhost"
PORT = 1883
DEVICE_ID = 1
TOPIC = f"iot/device/{DEVICE_ID}/telemetry"

client = mqtt.Client()
client.connect(BROKER, PORT, 60)

try:
    print(f"Starting telemetry publisher for device {DEVICE_ID}")
    print(f"Publishing to topic: {TOPIC}")
    print("Press Ctrl+C to stop...")
    
    while True:
        payload = {
            "temp": round(20 + random.random()*15, 2),  # 20-35°C
            "hum": round(40 + random.random()*30, 2),   # 40-70%
            "timestamp": int(time.time())
        }
        client.publish(TOPIC, json.dumps(payload), qos=1)
        print(f"Published: {payload}")
        time.sleep(3)
        
except KeyboardInterrupt:
    print("\nStopped")
finally:
    client.disconnect()