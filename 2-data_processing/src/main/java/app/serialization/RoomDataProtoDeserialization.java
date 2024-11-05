package app.serialization;

import app.proto.RoomDataProtos.RoomData;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class RoomDataProtoDeserialization implements DeserializationSchema<RoomData> {
    @Override
    public RoomData deserialize(byte[] message) throws IOException {
        try {
            return RoomData.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
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
