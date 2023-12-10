package org.vkartashov.log.service.replication;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vkartashov.log.controller.dto.LogEntry;
import org.vkartashov.log.repository.ReplicatedLogRepository;

@GRpcService
public class MessageServiceGrpcImpl extends LogReplicationServiceGrpc.LogReplicationServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(MessageServiceGrpcImpl.class);

    private final ReplicatedLogRepository repository;

    public MessageServiceGrpcImpl(ReplicatedLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void replicate(ReplicateRequest request, StreamObserver<ReplicateResponse> responseObserver) {
        LOG.info("Replicating " + request);
        repository.save(new LogEntry(request.getMessage()));
        responseObserver.onNext(ReplicateResponse.newBuilder().setReplicated(true).build());
        responseObserver.onCompleted();
        LOG.info("Replicated " + request);
    }

}
