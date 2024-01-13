package org.vkartashov.log.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vkartashov.log.controller.dto.SaveLogRequest;
import org.vkartashov.log.repository.ReplicatedLogRepository;

import java.text.MessageFormat;

@ConditionalOnExpression("!${replication-log.is-replica}")
@RestController
@RequestMapping("/api")
public class MasterLogController {

    public static final Logger LOG = LoggerFactory.getLogger(MasterLogController.class);

    private final ReplicatedLogRepository repository;

    public MasterLogController(ReplicatedLogRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/logs")
    public ResponseEntity<?> saveLogEntry(@RequestBody SaveLogRequest request) {
        LOG.info(MessageFormat.format("Executing request {0}", request));
        repository.save(request.getEntry(), request.getWriteConcern());
        LOG.info(MessageFormat.format("Finished executing request {0}", request));
        return ResponseEntity.ok().build();
    }

}
