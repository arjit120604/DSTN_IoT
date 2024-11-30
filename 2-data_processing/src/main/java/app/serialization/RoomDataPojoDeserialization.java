package app.serialization;

import app.models.RoomData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class RoomDataPojoDeserialization implements DeserializationSchema<RoomData> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RoomData deserialize(byte[] message) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            RoomData roomData = new RoomData();
            
            // Set room ID
            if (jsonNode.has("roomId")) {
                roomData.setRoomId(jsonNode.get("roomId").asText());
            } else {
                roomData.setRoomId("unknown");
            }

            // Set timestamp
            if (jsonNode.has("timestamp")) {
                roomData.setTimestamp(jsonNode.get("timestamp").asLong());
            }
            
            // Set time as needed
            
            // Set sensor values based on message type
            if (jsonNode.has("temperature")) {
                roomData.setTemperature(jsonNode.get("temperature").asDouble());
            }
            if (jsonNode.has("humidity")) {
                roomData.setHumidity(jsonNode.get("humidity").asDouble());
            }
            if (jsonNode.has("use")) {
                roomData.setEnergyConsumption(jsonNode.get("use").asDouble());
            }
            if (jsonNode.has("gen")) {
                roomData.setGen(jsonNode.get("gen").asDouble());
            }

            return roomData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Deserialization failed", e);
        }
    }

    @Override
    public boolean isEndOfStream(RoomData roomData) {
        return false;
    }

    @Override
    public TypeInformation<RoomData> getProducedType() {
        return TypeInformation.of(RoomData.class);
    }
}