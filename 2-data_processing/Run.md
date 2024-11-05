## Accessing the Flink CLI

- Finding name of the job manager pod
```sh
docker ps 
```
- Executing the Flink CLI
```sh
docker exec -it <job-manager-pod-name> /bin/bash
```
- To list all the jobs
```sh
flink list
```
- To cancel a job
```sh
flink cancel <job-id>
```
#### Note:
this a not a graceful shutdown. The job cannot be resumed after this.
####
- To pause a job graceful shutdown.
```sh
flink stop --savepointPath <savepoint-folder> <job-id>
```