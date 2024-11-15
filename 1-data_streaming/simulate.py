import pandas as pd
import time
from kafka import KafkaProducer
import json


# Load the dataset
dataset = pd.read_csv('./HomeC.csv')

# Kafka setup: configure the Kafka producer
producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    compression_type='gzip'  # Options: 'gzip', 'snappy', 'lz4', 'zstd'
)

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
        'time': row['time'],
        'temperature': row['temperature'],
    }
    producer.send(topics['temperature'], temp_data)

    humidity_data = {
        'time': row['time'],
        'humidity': row['humidity'],
    }
    producer.send(topics['humidity'], humidity_data)

    energy_data = {
        'time': row['time'],
        'use': row['use [kW]'],
        'gen': row['gen [kW]']
    }
    producer.send(topics['energy'], energy_data)

    print(f"Sent data - Temp: {temp_data}, Humidity: {humidity_data}, Energy: {energy_data}")

# Simulate the real-time sensor stream
for _, row in dataset.iterrows():
    simulate_sensor_data(row)
    time.sleep(1)  # Adjust the delay to simulate real-time streaming

# Close the producer after streaming
producer.close()
