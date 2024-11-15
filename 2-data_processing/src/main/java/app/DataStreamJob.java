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

import app.models.RoomData;
import app.proto.RoomDataProtos;
import app.serialization.RoomDataPojoDeserialization;
import app.serialization.RoomDataProtoDeserialization;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
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
	private static final String[] ROOM_TOPICS = {
			"room1-sensors",
			"room2-sensors",
			"room3-sensors",
			"room4-sensors"
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

		for (String topic : ROOM_TOPICS) {
			KafkaSource<RoomData> source = createRoomSource(topic, properties, deserializer);
			DataStream<RoomData> stream = env.fromSource(
					source,
					WatermarkStrategy.noWatermarks(),
					"Kafka Source - " + topic
			);

			resultStream = (resultStream == null) ? stream : resultStream.union(stream);
		}

		return resultStream;
	}

	private static void runJobPojo(StreamExecutionEnvironment env, Properties properties) {
		DeserializationSchema<RoomData> deserializer = new RoomDataPojoDeserialization();
		DataStream<RoomData> stream = createRoomStreams(env, properties, deserializer);

		DataStream<RoomData> allRoomsStream = stream.assignTimestampsAndWatermarks(
				WatermarkStrategy.<RoomData>forBoundedOutOfOrderness(Duration.ofSeconds(15))
						.withTimestampAssigner((roomData, timestamp) -> roomData.getTimestamp())
		);
		// processingTimeWindows: system clock
		// eventTimeWindows: timestamp in the data
		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(5), Duration.ofSeconds(30)))
				.process(new MovingAverage())
				.print();

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(1), Duration.ofSeconds(10L)))
				.process(new RapidChangeDetection())
				.print();

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(10), Duration.ofSeconds(30)))
				.process(new OccupancyDetector())
				.print();

		allRoomsStream
				.keyBy(RoomData::getRoomId)
				.window(SlidingEventTimeWindows.of(Duration.ofMinutes(15), Duration.ofMinutes(1))) // Longer window for FFT
				.process(new FFTAnomalyDetector())
				.print();
	}
	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("group.id", "flink-group");

		runJobPojo(env, properties);
		env.execute("Multi-Room Monitoring");
	}
}
