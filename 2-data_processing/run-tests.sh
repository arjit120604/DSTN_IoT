#!/bin/bash

# Start Kafka (if not already running)
docker-compose -f docker-compose-kafka-test.yml up -d

# Wait for Kafka to start
sleep 10

# Create topics
for room in {1..4}; do
    docker exec -it 2-data_processing-kafka-1 kafka-topics.sh \
        --create \
        --topic room${room}-sensors \
        --bootstrap-server localhost:9092 \
        --partitions 1 \
        --replication-factor 1 \
        2>/dev/null || true
done

# Compile and run the test data generator
mvn compile exec:java -Dexec.mainClass="app.utils.TestDataGenerator" | while read -r line; do
    for room in {1..4}; do
        if [[ $line == *"\"roomId\":\"room${room}\""* ]]; then
            echo "$line" | docker exec -i 2-data_processing-kafka-1 kafka-console-producer.sh \
                --topic room${room}-sensors \
                --bootstrap-server localhost:9092
        fi
    done
done

echo "Test data sent to Kafka topics"