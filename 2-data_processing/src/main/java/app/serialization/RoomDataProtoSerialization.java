package app.serialization;

import app.proto.RoomDataProtos.RoomData;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class RoomDataProtoSerialization implements SerializationSchema<RoomData> {
    @Override
    public byte[] serialize(RoomData roomData) {
        return roomData.toByteArray();
    }
}
