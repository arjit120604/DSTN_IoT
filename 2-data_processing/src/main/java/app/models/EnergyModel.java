package app.models;

public class EnergyModel {
    public String roomId;
    public long timestamp;
    public double use;
    public double gen;

    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getUse() {
        return use;
    }
    public void setUse(double use) {
        this.use = use;
    }
    public double getGen() {
        return gen;
    }
    public void setGen(double gen) {
        this.gen = gen;
    }
}
