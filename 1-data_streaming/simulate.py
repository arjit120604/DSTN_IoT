import pandas as pd
import time
from confluent_kafka import Producer
import json

# Load the dataset
dataset = pd.read_csv('./HomeC.csv', low_memory=False)

# Kafka setup: configure the Kafka producer
producer = Producer({
    'bootstrap.servers': 'localhost:9093',  # Kafka broker address
    'compression.type': 'gzip',
                 # Options: 'gzip', 'snappy', 'lz4', 'zstd'
})

# Define the topics for each sensor type
topics = {
    "temperature": "temperature_topic",
    "humidity": "humidity_topic",
    "energy": "energy_topic"
}

# Function to simulate real-time data streaming
def simulate_sensor_data(row):
    # Extract each type of sensor data and send to the respective Kafka topic
    temp_data = {
        'roomId': 'room1',
        'timestamp': row['time'],
        'temperature': row['temperature'],
    }
    producer.produce(topics['temperature'], value=json.dumps(temp_data))

    humidity_data = {
        'roomId': 'room1',
        'timestamp': row['time'],
        'humidity': row['humidity'],
    }
    producer.produce(topics['humidity'], value=json.dumps(humidity_data))

    energy_data = {
        'roomId': 'room1',
        'timestamp': row['time'],
        'use': row['use [kW]'],
        'gen': row['gen [kW]']
    }
    producer.produce(topics['energy'], value=json.dumps(energy_data))

    print(f"Sent data - Temp: {temp_data}, Humidity: {humidity_data}, Energy: {energy_data}")

    # Ensure messages are delivered
    producer.flush()

# Simulate the real-time sensor stream
for _, row in dataset.iterrows():
    simulate_sensor_data(row)
    time.sleep(1)  # Adjust the delay to simulate real-time streaming

# Close the producer after streaming
producer.flush()
