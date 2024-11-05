package app.models;

public class RoomData {
    private String roomId;
    private Double temperature;
    private Double humidity;
    private Double energyConsumption;
    private Long timestamp;

    public RoomData(){

    }
    public String getRoomId() {
        return roomId;
    }
    public Double getTemperature() {
        return temperature;
    }
    public Double getHumidity() {
        return humidity;
    }
    public Double getEnergyConsumption() {
        return energyConsumption;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }
    public void setEnergyConsumption(Double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RoomData{" +
                "roomId='" + roomId + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", energyConsumption=" + energyConsumption +
                ", timestamp=" + timestamp +
                '}';
    }
}
