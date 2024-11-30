from flask import Flask, request, jsonify
from flask_cors import CORS
import zlib
import json
import base64
import os
import csv

app = Flask(__name__)
CORS(app)

# In-memory data store (compressed)
compressed_store = {}
records_store = [] 

parent_directory = os.path.dirname(os.path.abspath(__file__))

# Path to the CSV file in the parent directory
csv_file_path = os.path.join(parent_directory, "../data.csv")

# Ensure the CSV file exists and has headers
if not os.path.exists(csv_file_path):
    with open(csv_file_path, mode='w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(["time","room", "temperature", "humidity", "person_present"])

def compress_data(data):
    """Compresses a dictionary into a byte string."""
    json_data = json.dumps(data).encode('utf-8')
    return zlib.compress(json_data)

def decompress_data(data):
    """Decompresses a byte string into a dictionary."""
    json_data = zlib.decompress(data)
    return json.loads(json_data.decode('utf-8'))

@app.route('/store', methods=['POST'])
def store_data():
    """
    Store sensor data (temperature, humidity, person present/not).
    Expected JSON format:
    {
        "time": 123456, 
        "room": "room1",
        "temperature": 22,
        "humidity": 50,
        "person_present": true
    }
    """
    try:
        # Get data from the request
        sensor_data = request.json
        time = sensor_data.get("time")
        room = sensor_data.get("room")
        temperature = sensor_data.get("temperature")
        humidity = sensor_data.get("humidity")
        person_present = sensor_data.get("person_present")
        
        if not time:
            return jsonify({"status": "error", "message": "Time identifier is required"}), 400
        
        # Compress the data for storage
        compressed_store[time] = compress_data(sensor_data)

        records_store.append({
            "time": time,
            "room": room,
            "temperature": temperature,
            "humidity": humidity,
            "person_present": person_present
        })

        with open(csv_file_path, mode='a', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow([time, room, temperature, humidity, person_present])

        return jsonify({"status": "success", "message": f"Data stored for {time}"}), 200
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500
 
@app.route('/retrieve', methods=['GET'])
def retrieve_data():
    """
    Retrieve sensor data for a specific room.
    Query Parameter: ?room=<room_name>
    """
    try:
        time_start = request.args.get("time_start")
        time_end = request.args.get("time_end")
        
        if not time_start or not time_end:
            return jsonify({"status": "error", "message": "Both time_start and time_end are required"}), 400

        # Convert to integers for comparison
        time_start = int(time_start)
        time_end = int(time_end)

        # Filter records within the given time range
        filtered_records = [
            record for record in records_store
            if time_start <= int(record["time"]) <= time_end
        ]

        if filtered_records:
            # Send data to Laptop 2
            laptop2_url = "http://<Laptop2_IP>:<Laptop2_Port>/receive"  # Replace with Laptop 2's actual IP and port
            response = request.post(laptop2_url, json={"data": filtered_records}, timeout=10)

            if response.status_code == 200:
                return jsonify({"status": "success", "data": filtered_records, "message": "Data sent to Laptop 2"}), 200
            else:
                return jsonify({"status": "error", "message": f"Failed to send data to Laptop 2: {response.text}"}), 500
        else:
            return jsonify({"status": "error", "message": "No data found in the specified range"}), 404
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/delete', methods=['DELETE'])
def delete_data():
    """
    Delete sensor data for a specific room.
    Query Parameter: ?room=<room_name>
    """
    try:
        time = request.args.get("time")
        if not time:
            return jsonify({"status": "error", "message": "Time identifier is required"}), 400
        
        if time in compressed_store:
            del compressed_store[time]
            return jsonify({"status": "success", "message": f"Data deleted for {time}"}), 200
        else:
            return jsonify({"status": "error", "message": "No data found for the specified time"}), 404
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
