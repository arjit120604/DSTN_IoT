package app;

import app.models.RoomData;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class MovingAverage extends ProcessWindowFunction<
        RoomData, Tuple3<String, Double, Double>, String, TimeWindow> {

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple3<String, Double, Double>> out) {
        double sumTemperature = 0.0;
        double sumHumidity = 0.0;
        long count = 0L;

        for (RoomData roomData : elements) {
            sumTemperature += roomData.getTemperature();
            sumHumidity += roomData.getHumidity();
            count++;
        }
        if (count == 0){
            return;
        }
        double avgTemperature = sumTemperature / count;
        double avgHumidity = sumHumidity / count;

        out.collect(new Tuple3<>(roomId, avgTemperature, avgHumidity));
    }
}
