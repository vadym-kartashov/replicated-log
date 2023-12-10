package org.vkartashov.log.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.repository.ReplicatedLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogController {

    private final ReplicatedLogRepository repository;

    public LogController(ReplicatedLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogEntry>> getLogEntries() {
        return ResponseEntity.ok(repository.getLogEntries());
    }

}