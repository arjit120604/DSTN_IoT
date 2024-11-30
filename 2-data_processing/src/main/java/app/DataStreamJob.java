/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app;

import app.joiner.RoomDataEnergyJoiner;
import app.joiner.TempHumidityJoiner;
import app.models.EnergyModel;
import app.models.HumidityModel;
import app.models.RoomData;
import app.models.TemperatureModel;
import app.proto.RoomDataProtos;
import app.serialization.RoomDataPojoDeserialization;
import app.serialization.RoomDataProtoDeserialization;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import java.time.Duration;
import java.util.Properties;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {
	private static final String[] SENSOR_TOPICS = {
		"temperature_topic",
		"humidity_topic",
		"energy_topic"
	};
	private static KafkaSource<RoomData> createRoomSource(String topic, Properties properties, DeserializationSchema<RoomData> deserializer) {
		return KafkaSource.<RoomData>builder()
				.setProperties(properties)
				.setTopics(topic)
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(deserializer)
				.build();
	}

	private static DataStream<RoomData> createRoomStreams(
			StreamExecutionEnvironment env,
			Properties properties,
			DeserializationSchema<RoomData> deserializer) {

		DataStream<RoomData> resultStream = null;

		for (String topic : SENSOR_TOPICS) {
			KafkaSource<RoomData> source = createRoomSource(topic, properties, deserializer);
			DataStream<RoomData> stream = env.fromSource(
					source,
					WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)),
					"Kafka Source - " + topic
			);

			resultStream = (resultStream == null) ? stream : resultStream.union(stream);
		}

		return resultStream;
	}

	private static void runJobPojo(DataStream<RoomData> allRoomsStream, StreamExecutionEnvironment env, Properties properties) {

		// processingTimeWindows: system clock
		// eventTimeWindows: timestamp in the data

//		allRoomsStream.print();

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(TumblingEventTimeWindows.of(Duration.ofMinutes(1), Duration.ofSeconds(5)))
				.process(new MovingAverage())
				.name("Moving Average")
				.print();

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(1), Duration.ofSeconds(10)))
				.process(new RapidChangeDetection())
				.print()
				.name("Rapid Change Detection");

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(1), Duration.ofSeconds(30)))
				.process(new OccupancyDetector())
				.name("Occupancy Detector")
				.print();

//		allRoomsStream
//				.keyBy(RoomData::getRoomId)
//				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(15), Duration.ofMinutes(1))) // Longer window for FFT
//				.process(new FFTAnomalyDetector())
//				.print();
	}

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "kafka:9092");
		properties.setProperty("group.id", "flink-group");

//		runJobPojo(env, properties);
		JsonDeserializationSchema<RoomData> jsonFormat=new JsonDeserializationSchema<>(RoomData.class);
//
		KafkaSource<RoomData> src = KafkaSource.<RoomData>builder()
				.setProperties(properties)
				.setTopics("temperature_topic")
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(jsonFormat)
				.build();
		KafkaSource<TemperatureModel> tempSrc = KafkaSource.<TemperatureModel>builder()
				.setProperties(properties)
				.setTopics("temperature_topic")
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(new JsonDeserializationSchema<>(TemperatureModel.class))
				.build();

		KafkaSource<HumidityModel> humiditySrc = KafkaSource.<HumidityModel>builder()
				.setProperties(properties)
				.setTopics("humidity_topic")
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(new JsonDeserializationSchema<>(HumidityModel.class))
				.build();
		KafkaSource<EnergyModel> energySrc = KafkaSource.<EnergyModel>builder()
				.setProperties(properties)
				.setTopics("energy_topic")
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(new JsonDeserializationSchema<>(EnergyModel.class))
				.build();

		DataStream<TemperatureModel> tempStream = env
				.fromSource(tempSrc, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofMillis(500)), "Temperature Source");

		DataStream<HumidityModel> humidityStream = env
				.fromSource(humiditySrc, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofMillis(500)), "Humidity Source");

		DataStream<EnergyModel> energyStream = env
				.fromSource(energySrc, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofMillis(500)), "Energy Source");

		DataStream<RoomData> combinedStream = tempStream
				.map(temp -> {
					RoomData data = new RoomData(temp.getRoomId(), temp.getTimestamp());
					data.setTemperature(temp.getTemperature());
					return data;
				})
				.keyBy(RoomData::getRoomId)
				.connect(humidityStream.keyBy(HumidityModel::getRoomId))
				.process(new TempHumidityJoiner())
				.keyBy(RoomData::getRoomId)
				.connect(energyStream.keyBy(EnergyModel::getRoomId))
				.process(new RoomDataEnergyJoiner());

		// Continue with your existing processing
		DataStream<RoomData> processedStream = combinedStream;
//		processedStream.print();
		runJobPojo(processedStream, env, properties);

		env.execute("Multi-Room Monitoring");
	}
}
