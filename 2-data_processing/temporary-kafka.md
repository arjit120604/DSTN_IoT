# Kafka Setup Guide for Flink Testing

This guide explains how to set up a temporary Kafka environment for testing the Flink application.

## 1. Create Kafka Configuration

Create a new file `docker-compose-kafka-test.yml`: (Already created)


## 2. Start Services
```sh
docker-compose -f docker-compose-kafka-test.yml up
```

## 3. Access Kafka and setup topic
    
```sh
    docker exec -it 2-data_processing-kafka-1 kafka-topics.sh \
    --create \
    --topic room1-sensors \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1

docker exec -it 2-data_processing-kafka-1 kafka-topics.sh \
    --create \
    --topic room2-sensors \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1

docker exec -it 2-data_processing-kafka-1 kafka-topics.sh \
    --create \
    --topic room3-sensors \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1

docker exec -it 2-data_processing-kafka-1 kafka-topics.sh \
    --create \
    --topic room4-sensors \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1
```
## 4. Produce test messages
```sh
    docker exec -it 2-data_processing-kafka-1 kafka-console-producer.sh \
    --topic input-topic \
    --bootstrap-server localhost:9092
```

## 5. Stop Services
```sh
docker-compose -f docker-compose-kafka-test.yml down
```
