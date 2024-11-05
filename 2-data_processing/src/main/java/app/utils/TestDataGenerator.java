package app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Random;

public class TestDataGenerator {
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static double generateTemperature() {
        // Normal room temperature range: 18-26°C with occasional outliers
        return 22 + random.nextGaussian() * 2;
    }

    private static double generateHumidity() {
        // Normal humidity range: 30-60% with occasional outliers
        return 45 + random.nextGaussian() * 7;
    }

    private static double generateEnergyConsumption() {
        // Base consumption 100W with variations
        return 100 + random.nextGaussian() * 20;
    }

    public static void main(String[] args) throws Exception {
        String[] roomIds = {"room1", "room2", "room3", "room4"};

        // Generate test cases for different scenarios
        System.out.println("# Normal Operation Test Cases");
        for (String roomId : roomIds) {
            for (int i = 0; i < 5; i++) {
                generateAndPrintTestCase(roomId, false, false);
            }
        }

        System.out.println("\n# Rapid Change Test Cases");
        // Temperature spike
        for (String roomId : roomIds) {
            double baseTemp = 22.0;
            for (int i = 0; i < 3; i++) {
                baseTemp += 1.5; // Increasing temperature
                generateAndPrintTestCase(roomId, true, false, baseTemp);
            }
        }

        System.out.println("\n# High Occupancy Test Cases");
        // High energy consumption and humidity variation
        for (String roomId : roomIds) {
            for (int i = 0; i < 3; i++) {
                generateAndPrintTestCase(roomId, false, true);
            }
        }
    }

    private static void generateAndPrintTestCase(String roomId, boolean rapidChange, boolean highOccupancy) {
        generateAndPrintTestCase(roomId, rapidChange, highOccupancy, null);
    }

    private static void generateAndPrintTestCase(String roomId, boolean rapidChange, boolean highOccupancy, Double fixedTemp) {
        try {
            double temperature = fixedTemp != null ? fixedTemp : generateTemperature();
            double humidity = generateHumidity();
            double energyConsumption = generateEnergyConsumption();

            if (rapidChange) {
                temperature += 3.0; // Add significant change
            }

            if (highOccupancy) {
                energyConsumption += 50.0; // Add significant energy consumption
                humidity += 10.0; // Add significant humidity
            }

            String json = String.format(
                    "{\"roomId\":\"%s\",\"temperature\":%.2f,\"humidity\":%.2f,\"energyConsumption\":%.2f,\"timestamp\":%d}",
                    roomId, temperature, humidity, energyConsumption, Instant.now().toEpochMilli()
            );

            System.out.println(json);
            Thread.sleep(100); // Add small delay between messages
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}