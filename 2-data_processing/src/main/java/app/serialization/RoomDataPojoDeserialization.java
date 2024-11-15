package app.serialization;

import app.models.RoomData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class RoomDataPojoDeserialization implements DeserializationSchema<RoomData> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RoomData deserialize(byte[] message) throws IOException {
        try {
            return objectMapper.readValue(message, RoomData.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Deserialization failed", e);
        }
    }

    @Override
    public boolean isEndOfStream(RoomData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<RoomData> getProducedType() {
        return TypeInformation.of(RoomData.class);
    }
}