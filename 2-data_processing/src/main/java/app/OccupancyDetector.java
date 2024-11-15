package app;

import app.models.RoomData;
import app.utils.Config;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class OccupancyDetector extends ProcessWindowFunction<
        RoomData, Tuple2<String, Boolean>, String, TimeWindow> {

    private static final double ENERGY_THRESHOLD = Config.Occupancy.ENERGY_THRESHOLD;
    private static final double HUMIDITY_THRESHOLD = Config.Occupancy.HUMIDITY_THRESHOLD;
    private static final double TEMPERATURE_THRESHOLD = Config.Occupancy.TEMPERATURE_THRESHOLD;

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple2<String, Boolean>> out) {

        double avgEnergy = 0.0;
        long count = 0L;
        double humidityVariation;
        double temperatureVariation;
        double maxHumidity = Double.MIN_VALUE;
        double minHumidity = Double.MAX_VALUE;
        double maxTemp = Double.MIN_VALUE;
        double minTemp = Double.MAX_VALUE;


        for (RoomData roomData: elements){
            avgEnergy += roomData.getEnergyConsumption();
            maxHumidity = Math.max(maxHumidity, roomData.getHumidity());
            minHumidity = Math.min(minHumidity, roomData.getHumidity());
            maxTemp = Math.max(maxTemp, roomData.getTemperature());
            minTemp = Math.min(minTemp, roomData.getTemperature());
            count++;
        }

        if (count == 0){
            return;
        }

        avgEnergy /= count;
        humidityVariation = maxHumidity - minHumidity;
        temperatureVariation = maxTemp - minTemp;
        boolean isOccupied = avgEnergy > ENERGY_THRESHOLD && (humidityVariation > HUMIDITY_THRESHOLD || temperatureVariation > TEMPERATURE_THRESHOLD);
        out.collect(new Tuple2<>(roomId, isOccupied));
    }
}
