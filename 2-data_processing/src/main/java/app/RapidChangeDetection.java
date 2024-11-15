package app;

import app.models.RoomData;
import app.utils.Config.RapidChange;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

// out collector : {roomId, isTemperatureChange, isEnergyChange}
public class RapidChangeDetection extends ProcessWindowFunction<RoomData, Tuple3<String, Boolean, Boolean>, String, TimeWindow> {

    private static final Double TEMPERATURE_THRESHOLD = RapidChange.TEMPERATURE_THRESHOLD;//2 celsius
    private static final Double ENERGY_THRESHOLD = RapidChange.ENERGY_THRESHOLD;//assuming
    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements, Collector<Tuple3<String, Boolean, Boolean>> out){
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

        if (maxEnergy - minEnergy >= ENERGY_THRESHOLD && maxTemp - minTemp >= TEMPERATURE_THRESHOLD){
            out.collect(new Tuple3<>(roomId, true, true));
        }
        else if (maxTemp - minTemp >= TEMPERATURE_THRESHOLD){
            out.collect(new Tuple3<>(roomId, true, false));
        }

        else if (maxEnergy - minEnergy >= ENERGY_THRESHOLD){
            out.collect(new Tuple3<>(roomId, false, true));
        }
    }
}
