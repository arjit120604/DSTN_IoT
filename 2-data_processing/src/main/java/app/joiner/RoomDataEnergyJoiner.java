package app.joiner;

import app.models.EnergyModel;
import app.models.RoomData;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

public class RoomDataEnergyJoiner extends CoProcessFunction<RoomData, EnergyModel, RoomData> {
    private ValueState<RoomData> roomState;
    private ValueState<EnergyModel> energyState;
    private static final long JOIN_WINDOW = 5000; // 5 second window

    @Override
    public void open(Configuration config) {
        roomState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("room-state", RoomData.class));
        energyState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("energy-state", EnergyModel.class));
    }

    @Override
    public void processElement1(RoomData room, Context ctx, Collector<RoomData> out) throws Exception {
        EnergyModel energy = energyState.value();
        if (energy != null && Math.abs(room.getTimestamp() - energy.getTimestamp()) <= JOIN_WINDOW) {
            room.setUse(energy.getUse());
            room.setGen(energy.getGen());
            out.collect(room);
            energyState.clear();
        } else {
            roomState.update(room);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + JOIN_WINDOW);
        }
    }

    @Override
    public void processElement2(EnergyModel energy, Context ctx, Collector<RoomData> out) throws Exception {
        RoomData room = roomState.value();
        if (room != null && Math.abs(room.getTimestamp() - energy.getTimestamp()) <= JOIN_WINDOW) {
            room.setUse(energy.getUse());
            room.setGen(energy.getGen());
            out.collect(room);
            roomState.clear();
        } else {
            energyState.update(energy);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + JOIN_WINDOW);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<RoomData> out) throws Exception {
        RoomData room = roomState.value();
        EnergyModel energy = energyState.value();

        if (room != null) {
            out.collect(room);
            roomState.clear();
        }
        if (energy != null) {
            RoomData partial = new RoomData(energy.getRoomId(), energy.getTimestamp());
            partial.setUse(energy.getUse());
            partial.setGen(energy.getGen());
            out.collect(partial);
            energyState.clear();
        }
    }
}