package app;

import app.models.RoomData;
import app.utils.Config.RapidChange;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class RapidChangeDetection extends ProcessWindowFunction<RoomData, Tuple3<String, Boolean, Boolean>, String, TimeWindow> {

    private static final double TEMPERATURE_THRESHOLD = RapidChange.TEMPERATURE_THRESHOLD;
    private static final double ENERGY_THRESHOLD = RapidChange.ENERGY_THRESHOLD;
    
    //cusum parameters
    private static final double K_TEMP = TEMPERATURE_THRESHOLD / 1.5; // Slack value for temperature
    private static final double K_ENERGY = ENERGY_THRESHOLD / 1.5;    // Slack value for energy
    private static final double H_TEMP = TEMPERATURE_THRESHOLD * 1.5; // Decision interval for temperature
    private static final double H_ENERGY = ENERGY_THRESHOLD * 1.5;    // Decision interval for energy

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements, 
                       Collector<Tuple3<String, Boolean, Boolean>> out) {
        
        List<Double> temperatures = new ArrayList<>();
        List<Double> energyValues = new ArrayList<>();

        for (RoomData roomData : elements) {
            temperatures.add(roomData.getTemperature());
            energyValues.add(roomData.getEnergyConsumption());
        }

        if (temperatures.isEmpty()) {
            return;
        }
        double meanTemp = calculateMean(temperatures);
        double meanEnergy = calculateMean(energyValues);

        boolean tempChange = detectCusumChange(temperatures, meanTemp, K_TEMP, H_TEMP);
        boolean energyChange = detectCusumChange(energyValues, meanEnergy, K_ENERGY, H_ENERGY);

        if (tempChange || energyChange) {
            out.collect(new Tuple3<>(roomId, tempChange, energyChange));
        }
    }

    private double calculateMean(List<Double> values) {
        return values.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
    }

    private boolean detectCusumChange(List<Double> values, double mean, double k, double h) {
        double cusumPos = 0;
        double cusumNeg = 0;
        
        for (double value : values) {
            double deviation = value - mean;
            cusumPos = Math.max(0, cusumPos + deviation - k);
            cusumNeg = Math.max(0, cusumNeg - deviation - k);
            if (cusumPos > h || cusumNeg > h) {
                return true;
            }
        }
        
        return false;
    }
}