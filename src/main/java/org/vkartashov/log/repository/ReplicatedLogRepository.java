package org.vkartashov.log.repository;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.service.replication.MessageServiceClient;
import org.vkartashov.log.service.replication.ReplicateRequest;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Repository
public class ReplicatedLogRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedLogRepository.class);

    private final List<MessageServiceClient> replicas;
    private final ExecutorService executorService;
    private final SortedSet<LogEntry> logEntries = new TreeSet<>(Comparator.comparingLong(LogEntry::getOrderNum));

    @Autowired
    public ReplicatedLogRepository(List<MessageServiceClient> replicas, ExecutorService replicationExecutorService) {
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
        synchronized (logEntries) {
            int orderNum = logEntries.size();
            logEntry.setOrderNum(orderNum);
            replicateMessage(logEntry, replicasToConfirm);
            logEntries.add(logEntry);
        }
        LOG.info("Saved " + logEntry);
    }

    @SneakyThrows
    private void replicateMessage(LogEntry entry, int replicasToConfirm) {
        CountDownLatch latch = new CountDownLatch(replicasToConfirm);
        for (MessageServiceClient serviceClient : replicas) {
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
        return List.copyOf(logEntries);
    }

}
