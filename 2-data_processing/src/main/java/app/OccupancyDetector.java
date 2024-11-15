package app;

import app.models.RoomData;
import app.utils.Config;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class OccupancyDetector extends ProcessWindowFunction<
        RoomData, Tuple2<String, Boolean>, String, TimeWindow> {

    private static final double ENERGY_THRESHOLD = Config.Occupancy.ENERGY_THRESHOLD;
    private static final double HUMIDITY_THRESHOLD = Config.Occupancy.HUMIDITY_THRESHOLD;
    private static final double TEMPERATURE_THRESHOLD = Config.Occupancy.TEMPERATURE_THRESHOLD;

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple2<String, Boolean>> out) {

        double sumEnergy = 0.0;
        List<Double> humidityList = new ArrayList<>();
        List<Double> temperatureList = new ArrayList<>();
        long count = 0L;

        for (RoomData roomData : elements) {
            sumEnergy += roomData.getEnergyConsumption();
            humidityList.add(roomData.getHumidity());
            temperatureList.add(roomData.getTemperature());
            count++;
        }

        if (count == 0) {
            return;
        }

        double avgEnergy = sumEnergy / count;
        double stdDevHumidity = calculateStdDev(humidityList, calculateMean(humidityList));
        double stdDevTemperature = calculateStdDev(temperatureList, calculateMean(temperatureList));

        boolean isOccupied = avgEnergy > ENERGY_THRESHOLD &&
                (stdDevHumidity > HUMIDITY_THRESHOLD || stdDevTemperature > TEMPERATURE_THRESHOLD);

        out.collect(new Tuple2<>(roomId, isOccupied));
    }

    private double calculateMean(List<Double> data) {
        double sum = 0.0;
        for (Double d : data) {
            sum += d;
        }
        return sum / data.size();
    }

    private double calculateStdDev(List<Double> data, double mean) {
        double sum = 0.0;
        for (Double d : data) {
            sum += Math.pow(d - mean, 2);
        }
        return Math.sqrt(sum / data.size());
    }
}