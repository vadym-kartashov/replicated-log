package org.vkartashov.log.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.service.replication.MessageServiceClient;
import org.vkartashov.log.service.replication.ReplicateRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ReplicatedLogRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedLogRepository.class);

    private final List<MessageServiceClient> replicas;
    private final List<LogEntry> logEntries = new ArrayList<>();

    @Autowired
    public ReplicatedLogRepository(List<MessageServiceClient> replicas) {
        this.replicas = replicas;
    }

    public void save(LogEntry logEntry) {
        LOG.info("Saving " + logEntry);
        for (MessageServiceClient serviceClient : replicas) {
            serviceClient.replicate(
                    ReplicateRequest.newBuilder()
                            .setMessage(logEntry.getMessage())
                            .build()
            );
        }
        logEntries.add(logEntry);
        LOG.info("Saved " + logEntry);
    }

    public List<LogEntry> getLogEntries() {
        LOG.info("Get log entries");
        return Collections.unmodifiableList(logEntries);
    }

}
