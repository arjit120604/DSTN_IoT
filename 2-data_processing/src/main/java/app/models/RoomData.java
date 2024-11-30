package app.models;

public class RoomData {
    private Double temperature;
    private Double humidity;
    private Double use;
    private Double gen;
    private String roomId;
    private Long timestamp;

    public RoomData() {
    }
    public RoomData(String roomId, long timestamp){
        this.roomId = roomId;
        this.timestamp = timestamp;
    }
    public boolean isComplete(){
        return this.temperature != null && this.humidity != null && this.use != null && this.gen != null;
    }
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getUse() {
        return use;
    }

    public void setUse(Double use) {
        this.use = use;
        this.setEnergyConsumption(use);
    }

    public Double getGen() {
        return gen;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public void setGen(Double gen) {
        this.gen = gen;
    }
    public Double getEnergyConsumption() {
        return use;
    }
    public void setEnergyConsumption(Double use) {
        this.use = use;
    }
    @Override
    public String toString() {
        return "RoomData{" +
                "timestamp='" + timestamp + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", use=" + use +
                ", gen=" + gen +
                '}';
    }
}