package app.joiner;

import app.models.HumidityModel;
import app.models.RoomData;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

public class TempHumidityJoiner extends CoProcessFunction<RoomData, HumidityModel, RoomData> {
    private ValueState<RoomData> tempState;
    private ValueState<HumidityModel> humidityState;
    private static final long JOIN_WINDOW = 5000; // 5 second window

    @Override
    public void open(Configuration config) {
        tempState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("temp-state", RoomData.class));
        humidityState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("humidity-state", HumidityModel.class));
    }

    @Override
    public void processElement1(RoomData temp, Context ctx, Collector<RoomData> out) throws Exception {
        HumidityModel humidity = humidityState.value();
        if (humidity != null && Math.abs(temp.getTimestamp() - humidity.getTimestamp()) <= JOIN_WINDOW) {
            temp.setHumidity(humidity.getHumidity());
            out.collect(temp);
            humidityState.clear();
        } else {
            tempState.update(temp);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + JOIN_WINDOW);
        }
    }

    @Override
    public void processElement2(HumidityModel humidity, Context ctx, Collector<RoomData> out) throws Exception {
        RoomData temp = tempState.value();
        if (temp != null && Math.abs(temp.getTimestamp() - humidity.getTimestamp()) <= JOIN_WINDOW) {
            temp.setHumidity(humidity.getHumidity());
            out.collect(temp);
            tempState.clear();
        } else {
            humidityState.update(humidity);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + JOIN_WINDOW);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<RoomData> out) throws Exception {
        // Emit partial data and clear state
        RoomData temp = tempState.value();
        HumidityModel humidity = humidityState.value();

        if (temp != null) {
            out.collect(temp);
            tempState.clear();
        }
        if (humidity != null) {
            RoomData partial = new RoomData(humidity.getRoomId(), humidity.getTimestamp());
            partial.setHumidity(humidity.getHumidity());
            out.collect(partial);
            humidityState.clear();
        }
    }
}