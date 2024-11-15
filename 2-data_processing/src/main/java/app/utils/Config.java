package app.utils;


public class Config {
    public static class FFT{
        public static final double ANOMALY_THRESHOLD = 1.5;
    }
    public static class RapidChange{
        public static final double TEMPERATURE_THRESHOLD = 2.0;//
        public static final double ENERGY_THRESHOLD = 15.0;
    }

    public static class Occupancy{
        public static final double ENERGY_THRESHOLD = 1.5;
        public static final double HUMIDITY_THRESHOLD = 5;
        public static final double TEMPERATURE_THRESHOLD = 1;
    }

}
