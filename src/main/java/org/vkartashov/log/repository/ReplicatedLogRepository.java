package org.vkartashov.log.repository;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.service.replication.MessageReplicaServiceClient;
import org.vkartashov.log.service.replication.ReplicateRequest;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Repository
public class ReplicatedLogRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedLogRepository.class);

    private final List<MessageReplicaServiceClient> replicas;
    private final ExecutorService executorService;
    private final SortedMap<Long, LogEntry> logEntriesIndex = new ConcurrentSkipListMap<>();

    @Autowired
    public ReplicatedLogRepository(List<MessageReplicaServiceClient> replicas, ExecutorService replicationExecutorService) {
        this.replicas = replicas;
        this.executorService = replicationExecutorService;
    }

    public void save(LogEntry logEntry, int writeConcern) {
        Assert.notNull(logEntry, "Log entry should not be null");
        Assert.notNull(logEntry.getMessage(), "Log entry message should not be null");
        Assert.isTrue(writeConcern >= 1, "Write concern should be >= 1");
        int replicasToConfirm = writeConcern - 1;
        Assert.isTrue(replicasToConfirm <= replicas.size(),
                "Not enough replicas configured to support write concern %d".formatted(writeConcern));

        LOG.info("Saving " + logEntry);
        if (logEntry.getOrderNum() == null) {
            logEntry.setOrderNum(logEntriesIndex.size());
        }
        int orderNum = logEntry.getOrderNum();
        replicateMessage(logEntry, replicasToConfirm);
        logEntriesIndex.put((long) orderNum, logEntry);
        LOG.info("Saved " + logEntry);
    }

    @SneakyThrows
    private void replicateMessage(LogEntry entry, int replicasToConfirm) {
        CountDownLatch latch = new CountDownLatch(replicasToConfirm);
        for (MessageReplicaServiceClient serviceClient : replicas) {
            executorService.execute(() -> {
                serviceClient.replicate(
                        ReplicateRequest.newBuilder()
                                .setMessage(entry.getMessage())
                                .setOrderNum(entry.getOrderNum())
                                .build());
                latch.countDown();
            });
        }
        latch.await();
    }

    public List<LogEntry> getLogEntries() {
        LOG.info("Get log entries");
        return List.copyOf(logEntriesIndex.values());
    }

    public Long getLastOrderNum() {
        return logEntriesIndex.lastKey();
    }

}
