package org.vkartashov.log.service.replication;

import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.repository.ReplicatedLogRepository;
import org.vkartashov.log.util.LogProtoUtil;

import java.text.MessageFormat;

@GRpcService
public class MessageReplicaServiceGrpcImpl extends LogReplicationServiceGrpc.LogReplicationServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReplicaServiceGrpcImpl.class);

    private final ReplicatedLogRepository repository;
    private final int replicationDelay;

    public MessageReplicaServiceGrpcImpl(
            ReplicatedLogRepository repository,
            @Value("${replication-log.replication-delay:0}") int replicationDelay) {
        this.repository = repository;
        this.replicationDelay = replicationDelay;
    }

    @Override
    @SneakyThrows
    public void replicate(ReplicateRequest request, StreamObserver<ReplicateResponse> responseObserver) {
        LOG.info(MessageFormat.format("Replicating {0}", LogProtoUtil.toString(request)));
        delayReplication();
        while ((repository.getLastOrderNum() == null && request.getOrderNum() > 0) ||
                (repository.getLastOrderNum() != null && repository.getLastOrderNum() + 1 < request.getOrderNum())) {
            LOG.info(MessageFormat.format("Waiting for orderNum {0}", request.getOrderNum() - 1));
            Thread.sleep(1000);
        }
        repository.save(new LogEntry(request.getMessage(), request.getOrderNum()), 1);
        responseObserver.onNext(ReplicateResponse.newBuilder().setReplicated(true).build());
        responseObserver.onCompleted();
        LOG.info(MessageFormat.format("Replicated {0}", LogProtoUtil.toString(request)));
    }

    @Override
    public void getReplicationState(GetReplicationStateRequest request, StreamObserver<GetReplicationStateResponse> responseObserver) {
        Long lastOrderNum = repository.getLastOrderNum();
        LOG.info(MessageFormat.format("Getting replication state: {0}", lastOrderNum));
    }

    private void delayReplication() throws InterruptedException {
        if (replicationDelay == 0) {
            return;
        }
        LOG.info(MessageFormat.format("Sleeping for {0} ms", replicationDelay));
        Thread.sleep(replicationDelay);
    }

}
