package app;

import app.models.RoomData;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class OccupancyDetector extends ProcessWindowFunction<
        RoomData, Tuple2<String, Boolean>, String, TimeWindow> {

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple2<String, Boolean>> out) {

        double avgEnergyComsumption = 0.0;
        long count = 0L;
        double humidityVariation = 0.0;
        double temperatureVariation = 0.0;

        for (RoomData roomData: elements){
            avgEnergyComsumption += roomData.getEnergyConsumption();
            humidityVariation += roomData.getHumidity();
            temperatureVariation += roomData.getTemperature();
            count++;
        }

    }
}
