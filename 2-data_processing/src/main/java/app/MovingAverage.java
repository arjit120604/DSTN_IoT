package app;

import app.models.RoomData;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovingAverage extends ProcessWindowFunction<
        RoomData, Tuple4<String, Double, Double, Double>, String, TimeWindow> {

    public static final Logger LOG = LoggerFactory.getLogger(MovingAverage.class);
    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple4<String, Double, Double, Double>> out) {
        double sumTemperature = 0.0;
        double sumHumidity = 0.0;
        long count = 0L;
        double avgUse = 0.0;

        System.out.println("Processing window for room {}: {} to {}"+
                roomId+
                context.window().getStart()+
                context.window().getEnd());
        for (RoomData roomData : elements) {
            sumTemperature += roomData.getTemperature();
            sumHumidity += roomData.getHumidity();
            count++;
            avgUse += roomData.getUse();
        }
        if (count == 0){
            out.collect(new Tuple4<>(roomId, 0.0, 0.0, 0.0));
            return;
        }
        double avgTemperature = sumTemperature / count;
        double avgHumidity = sumHumidity / count;
        avgUse = avgUse / count;

        LOG.info("Room {}: Avg Temp = {}, Avg Humidity = {}",
                roomId, avgTemperature, avgHumidity);
        out.collect(new Tuple4<>(roomId, avgTemperature, avgHumidity, avgUse));
    }
}
