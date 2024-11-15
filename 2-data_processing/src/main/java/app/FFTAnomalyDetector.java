package app;

import app.models.RoomData;
import app.utils.Config;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.TransformType;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFTAnomalyDetector extends ProcessWindowFunction
        <RoomData, Tuple2<String, Boolean>, String, TimeWindow> {

    private static final double ANOMALY_THRESHOLD = Config.FFT.ANOMALY_THRESHOLD;

    private double[] convertToArray(Iterable<RoomData> elements) {
        List<Double> values = new ArrayList<>();
        for (RoomData data : elements) {
            values.add(data.getTemperature());
        }

        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }

        int nextPowerOf2 = Integer.highestOneBit(result.length);
        nextPowerOf2 = (nextPowerOf2 == result.length) ? nextPowerOf2 : nextPowerOf2 << 1;
        if (result.length != nextPowerOf2) {
            double[] padded = new double[nextPowerOf2];
            System.arraycopy(result, 0, padded, 0, result.length);
            return padded;
        }

        return result;
    }

    private boolean detectAnomalies(Complex[] fftResult) {
        double[] magnitudes = Arrays.stream(fftResult)
                .mapToDouble(Complex::abs)
                .toArray();

        double mean = Arrays.stream(magnitudes).average().orElse(0.0);
        double stdDev = Math.sqrt(Arrays.stream(magnitudes)
                .map(val -> Math.pow(val - mean, 2))
                .average()
                .orElse(0.0));

        for (double magnitude : magnitudes) {
            double zScore = (magnitude - mean) / stdDev;
            if (Math.abs(zScore) > ANOMALY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(String roomId, Context context, Iterable<RoomData> elements,
                        Collector<Tuple2<String, Boolean>> out) {

        double[] timeSeriesData = convertToArray(elements);
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResult = transformer.transform(timeSeriesData, TransformType.FORWARD);

        boolean isAnomaly = detectAnomalies(fftResult);
        if (isAnomaly){
            out.collect(new Tuple2<>(roomId, true));
        }
    }
}