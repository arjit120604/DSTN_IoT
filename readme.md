

## To submit flink job
```bash
docker exec -it $(docker ps | grep jobmanager | awk '{print $1}') flink run /opt/flink/usrlib/2-data_processing-1.0-SNAPSHOT.jar
```
## To cancel flink job
```bash
docker ps | grep jobmanager

docker exec -it flink-jobmanager flink cancel <job-id>
```

## To copy the logs
```bash
docker-compose logs taskmanager > ./logs/taskmanager/docker-taskmanager.log
```