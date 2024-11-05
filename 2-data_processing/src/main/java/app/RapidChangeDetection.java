package app;

import app.models.RoomData;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class RapidChangeDetection extends ProcessWindowFunction<RoomData, Tuple2<String, String>, String, TimeWindow> {

    private static final Double TEMPERATURE_THRESHOLD = 2.0;//2 celsius
    private static final Double ENERGY_THRESHOLD = 15.0;//assuming
    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements, Collector<Tuple2<String, String>> out){
        double minTemp = Double.MAX_VALUE;
        double maxTemp = Double.MIN_VALUE;
        double minEnergy = Double.MAX_VALUE;
        double maxEnergy = Double.MIN_VALUE;

        for(RoomData roomData: elements){
            minTemp = Math.min(minTemp, roomData.getTemperature());
            maxTemp = Math.max(maxTemp, roomData.getTemperature());
            minEnergy = Math.min(minEnergy, roomData.getEnergyConsumption());
            maxEnergy = Math.max(maxEnergy, roomData.getEnergyConsumption());
        }

        if (maxTemp - minTemp >= TEMPERATURE_THRESHOLD){
            out.collect(new Tuple2<>(roomId, "Rapid Temperature change detected"));
        }

        if (maxEnergy - minEnergy >= ENERGY_THRESHOLD){
            out.collect(new Tuple2<>(roomId, "Rapid Energy consumption change detected"));
        }
    }
}
