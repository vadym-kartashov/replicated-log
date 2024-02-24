# replicated-log iteration 2
## Requirements

The current iteration should provide tunable semi-synchronicity for replication with a retry mechanism that should deliver all messages exactly-once in total order. </br>

Main features (maximum 20 points):
* If message delivery fails (due to connection, or internal server error, or secondary is unavailable) the delivery attempts should be repeated - retry </br>
  If one of the secondaries is down and w=3, the client should be blocked until the node becomes available. Clients running in parallel shouldn’t be blocked by the blocked one.
If w>1 the client should be blocked until the message will be delivered to all secondaries required by the write concern level. Clients running in parallel shouldn’t be blocked by the blocked one.</br>
All messages that secondaries have missed due to unavailability should be replicated after (re)joining the master</br>
Retries can be implemented with an unlimited number of attempts but, possibly, with some “smart” delays logic</br>
You can specify a timeout for the master in case if there is no response from the secondary</br>
* All messages should be present exactly once in the secondary log - deduplication </br>
To test deduplication you can generate some random internal server error response from the secondary after the message has been added to the log
* The order of messages should be the same in all nodes - total order </br>
If secondary has received messages [msg1, msg2, msg4], it shouldn’t display the message ‘msg4’ until the ‘msg3’ will be received
To test the total order, you can generate some random internal server error response from the secondaries

## Development
Ordering number has been introduced to ensure correct ordering on replicas. All log entries are put into TreeSet with ordering through order number. </br>
Replica awaits replication request with sequential number. </br>
Retries are done with 
## How to run
Java 17 is required to be installed on local environment </br>
Run with command for Mac/Linux:
```bash
./up.sh
```

## Results
Scenario:
1. Down replica 2
2. Msg with w=1 is sent to master
3. Msg with w=2 is sent to master
4. Msg with w=3 is sent to master
5. Msg with w=1 is sent to master
6. Up replica 2
7. Check messages and order on all instances

Log where replica 2 replicates all the messages in right order is below:
```log
master-1    | 2024-02-24T18:58:42.838Z  INFO 1 --- [lication-pool-7] o.v.l.s.r.MessageReplicaServiceClient    : Replicating MSG4 3 to replica2:6565
replica2-1  | 2024-02-24T18:58:42.999Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicating MSG4 3
replica2-1  | 2024-02-24T18:58:43.000Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:44.002Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:45.008Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:46.014Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:47.023Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:48.042Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:49.048Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:50.054Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:51.057Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
master-1    | 2024-02-24T18:58:52.044Z  INFO 1 --- [lication-pool-1] o.v.l.s.r.MessageReplicaServiceClient    : Replicating MSG 0 to replica2:6565
replica2-1  | 2024-02-24T18:58:52.061Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:52.063Z  INFO 1 --- [ault-executor-2] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicating MSG 0
replica2-1  | 2024-02-24T18:58:52.065Z  INFO 1 --- [ault-executor-2] o.v.l.r.ReplicatedLogRepository          : Saving LogEntry(message=MSG, orderNum=0)
replica2-1  | 2024-02-24T18:58:52.066Z  INFO 1 --- [ault-executor-2] o.v.l.r.ReplicatedLogRepository          : Saved LogEntry(message=MSG, orderNum=0)
replica2-1  | 2024-02-24T18:58:52.106Z  INFO 1 --- [ault-executor-2] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicated MSG 0
master-1    | 2024-02-24T18:58:52.116Z  INFO 1 --- [lication-pool-1] o.v.l.s.r.MessageReplicaServiceClient    : Replicated MSG 0 to replica2:6565
replica2-1  | 2024-02-24T18:58:53.068Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
master-1    | 2024-02-24T18:58:53.604Z  INFO 1 --- [lication-pool-3] o.v.l.s.r.MessageReplicaServiceClient    : Replicating MSG2 1 to replica2:6565
replica2-1  | 2024-02-24T18:58:53.630Z  INFO 1 --- [ault-executor-2] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicating MSG2 1
replica2-1  | 2024-02-24T18:58:53.630Z  INFO 1 --- [ault-executor-2] o.v.l.r.ReplicatedLogRepository          : Saving LogEntry(message=MSG2, orderNum=1)
replica2-1  | 2024-02-24T18:58:53.631Z  INFO 1 --- [ault-executor-2] o.v.l.r.ReplicatedLogRepository          : Saved LogEntry(message=MSG2, orderNum=1)
replica2-1  | 2024-02-24T18:58:53.632Z  INFO 1 --- [ault-executor-2] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicated MSG2 1
master-1    | 2024-02-24T18:58:53.636Z  INFO 1 --- [lication-pool-3] o.v.l.s.r.MessageReplicaServiceClient    : Replicated MSG2 1 to replica2:6565
replica2-1  | 2024-02-24T18:58:54.071Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
replica2-1  | 2024-02-24T18:58:55.076Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Waiting for orderNum 2
master-1    | 2024-02-24T18:58:55.596Z  INFO 1 --- [lication-pool-5] o.v.l.s.r.MessageReplicaServiceClient    : Replicating MSG3 2 to replica2:6565
replica2-1  | 2024-02-24T18:58:55.617Z  INFO 1 --- [ault-executor-1] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicating MSG3 2
replica2-1  | 2024-02-24T18:58:55.619Z  INFO 1 --- [ault-executor-1] o.v.l.r.ReplicatedLogRepository          : Saving LogEntry(message=MSG3, orderNum=2)
replica2-1  | 2024-02-24T18:58:55.620Z  INFO 1 --- [ault-executor-1] o.v.l.r.ReplicatedLogRepository          : Saved LogEntry(message=MSG3, orderNum=2)
replica2-1  | 2024-02-24T18:58:55.623Z  INFO 1 --- [ault-executor-1] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicated MSG3 2
master-1    | 2024-02-24T18:58:55.627Z  INFO 1 --- [lication-pool-5] o.v.l.s.r.MessageReplicaServiceClient    : Replicated MSG3 2 to replica2:6565
master-1    | 2024-02-24T18:58:55.628Z  INFO 1 --- [nio-8080-exec-5] o.v.l.r.ReplicatedLogRepository          : Saved LogEntry(message=MSG3, orderNum=2)
master-1    | 2024-02-24T18:58:55.629Z  INFO 1 --- [nio-8080-exec-5] o.v.log.controller.MasterLogController   : Finished executing request SaveLogRequest(entry=LogEntry(message=MSG3, orderNum=2), writeConcern=3)
replica2-1  | 2024-02-24T18:58:56.084Z  INFO 1 --- [ault-executor-0] o.v.l.r.ReplicatedLogRepository          : Saving LogEntry(message=MSG4, orderNum=3)
replica2-1  | 2024-02-24T18:58:56.087Z  INFO 1 --- [ault-executor-0] o.v.l.r.ReplicatedLogRepository          : Saved LogEntry(message=MSG4, orderNum=3)
replica2-1  | 2024-02-24T18:58:56.090Z  INFO 1 --- [ault-executor-0] o.v.l.s.r.MessageReplicaServiceGrpcImpl  : Replicated MSG4 3
master-1    | 2024-02-24T18:58:56.103Z  INFO 1 --- [lication-pool-7] o.v.l.s.r.MessageReplicaServiceClient    : Replicated MSG4 3 to replica2:6565
master-1    | 2024-02-24T18:59:17.438Z  INFO 1 --- [nio-8080-exec-9] o.v.l.r.ReplicatedLogRepository          : Get log entries
replica1-1  | 2024-02-24T18:59:23.443Z  INFO 1 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
replica1-1  | 2024-02-24T18:59:23.444Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
replica1-1  | 2024-02-24T18:59:23.445Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
replica1-1  | 2024-02-24T18:59:23.494Z  INFO 1 --- [nio-8080-exec-1] o.v.l.r.ReplicatedLogRepository          : Get log entries
replica2-1  | 2024-02-24T18:59:26.307Z  INFO 1 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
replica2-1  | 2024-02-24T18:59:26.307Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
replica2-1  | 2024-02-24T18:59:26.309Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
replica2-1  | 2024-02-24T18:59:26.366Z  INFO 1 --- [nio-8080-exec-1] o.v.l.r.ReplicatedLogRepository          : Get log entries

```