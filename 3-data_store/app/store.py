from flask import Flask, request, jsonify
import zlib
import json
import base64
from flask_cors import CORS

app = Flask(__name__)
CORS(app)
# In-memory data store (compressed)
compressed_store = {}

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
        "room": "room1",
        "temperature": 22,
        "humidity": 50,
        "person_present": true
    }
    """
    try:
        # Get data from the request
        sensor_data = request.json
        room = sensor_data.get("room")
        
        if not room:
            return jsonify({"status": "error", "message": "Room identifier is required"}), 400
        
        # Compress the data for storage
        compressed_store[room] = compress_data(sensor_data)
        return jsonify({"status": "success", "message": f"Data stored for {room}"}), 200
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500
 
@app.route('/retrieve', methods=['GET'])
def retrieve_data():
    """
    Retrieve sensor data for a specific room.
    Query Parameter: ?room=<room_name>
    """
    try:
        room = request.args.get("room")
        if not room:
            return jsonify({"status": "error", "message": "Room identifier is required"}), 400
        
        if room in compressed_store:
            # Decompress and return the data
            decompressed_data = decompress_data(compressed_store[room])
            return jsonify({"status": "success", "data": decompressed_data}), 200
        else:
            return jsonify({"status": "error", "message": "No data found for the specified room"}), 404
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/delete', methods=['DELETE'])
def delete_data():
    """
    Delete sensor data for a specific room.
    Query Parameter: ?room=<room_name>
    """
    try:
        room = request.args.get("room")
        if not room:
            return jsonify({"status": "error", "message": "Room identifier is required"}), 400
        
        if room in compressed_store:
            del compressed_store[room]
            return jsonify({"status": "success", "message": f"Data deleted for {room}"}), 200
        else:
            return jsonify({"status": "error", "message": "No data found for the specified room"}), 404
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8080)
