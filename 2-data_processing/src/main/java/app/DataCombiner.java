package app;


import app.models.EnergyModel;
import app.models.HumidityModel;
import app.models.RoomData;
import app.models.TemperatureModel;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.protocol.types.Field;

import java.util.Map;
import java.util.HashMap;

public class DataCombiner extends KeyedProcessFunction<String, Object, RoomData>{
    private ValueState<TemperatureModel> tempState;
    private ValueState<HumidityModel> humidityState;
    private ValueState<EnergyModel> energyState;
    private ValueState<Long> timerState;

    private static final long TIME_WINDOW = 4000;

    @Override
    public void open(Configuration parameters) {
        tempState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("temp-state", TemperatureModel.class));
        humidityState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("humidity-state", HumidityModel.class));
        energyState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("energy-state", EnergyModel.class));
        timerState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("timer-state", Long.class));
    }
    @Override
    public void processElement(Object value, Context ctx, Collector<RoomData> out) throws Exception {
        // Register timer on first element
        if (timerState.value() == null) {
            long timer = ctx.timerService().currentProcessingTime() + TIME_WINDOW;
            ctx.timerService().registerProcessingTimeTimer(timer);
            timerState.update(timer);
        }

        if (value instanceof TemperatureModel) {
            tempState.update((TemperatureModel) value);
        } else if (value instanceof HumidityModel) {
            humidityState.update((HumidityModel) value);
        } else if (value instanceof EnergyModel) {
            energyState.update((EnergyModel) value);
        }

        // Try to combine and emit if we have all data
        tryToCombineAndEmit(out);
    }

    private void tryToCombineAndEmit(Collector<RoomData> out) throws Exception {
        TemperatureModel temp = tempState.value();
        HumidityModel humidity = humidityState.value();
        EnergyModel energy = energyState.value();

        if (temp != null && humidity != null && energy != null) {
            RoomData combined = new RoomData(temp.getRoomId(), temp.getTimestamp());
            combined.setTemperature(temp.getTemperature());
            combined.setHumidity(humidity.getHumidity());
            combined.setUse(energy.getUse());
            combined.setGen(energy.getGen());

            // Clear all states after successful combination
            tempState.clear();
            humidityState.clear();
            energyState.clear();
            timerState.clear();

            out.collect(combined);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<RoomData> out) throws Exception {
        // Emit partial data if timer expires
        TemperatureModel temp = tempState.value();
        HumidityModel humidity = humidityState.value();
        EnergyModel energy = energyState.value();

        if (temp != null || humidity != null || energy != null) {
            RoomData partial = new RoomData();
            if (temp != null) {
                partial.setRoomId(temp.getRoomId());
                partial.setTimestamp(temp.getTimestamp());
                partial.setTemperature(temp.getTemperature());
            }
            if (humidity != null) {
                partial.setHumidity(humidity.getHumidity());
            }
            if (energy != null) {
                partial.setUse(energy.getUse());
                partial.setGen(energy.getGen());
            }

            out.collect(partial);
        }

        // Clear all states
        tempState.clear();
        humidityState.clear();
        energyState.clear();
        timerState.clear();
    }
}
