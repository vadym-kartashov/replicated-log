package org.vkartashov.log.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.repository.ReplicatedLogRepository;

@ConditionalOnExpression("!${replication-log.is-replica}")
@Controller
@RequestMapping("/api")
public class MasterLogController {

    private final ReplicatedLogRepository repository;

    public MasterLogController(ReplicatedLogRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/logs")
    public ResponseEntity<?> saveLogEntry(@RequestBody LogEntry entry) {
        repository.save(entry);
        return ResponseEntity.ok().build();
    }

}
