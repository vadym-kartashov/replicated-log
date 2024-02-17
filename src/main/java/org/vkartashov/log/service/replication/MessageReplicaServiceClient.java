package org.vkartashov.log.service.replication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vkartashov.log.controller.dto.ReplicationStateDto;
import org.vkartashov.log.util.LogProtoUtil;

import java.text.MessageFormat;


public class MessageReplicaServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReplicaServiceClient.class);

    private LogReplicationServiceGrpc.LogReplicationServiceBlockingStub stub;
    @Getter
    private final String host;

    public MessageReplicaServiceClient(String host) {
        this.host = host;
    }

    public void init() {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(host)
                .usePlaintext()
                .build();
        stub = LogReplicationServiceGrpc.newBlockingStub(channel);
    }

    public boolean replicate(ReplicateRequest request) {
        LOG.info(MessageFormat.format("Replicating {0} to {1}", LogProtoUtil.toString(request), host));
        ReplicateResponse response = stub.replicate(
                ReplicateRequest.newBuilder()
                        .setMessage(request.getMessage())
                        .build()
        );
        LOG.info(MessageFormat.format("Replicated {0} to {1}", LogProtoUtil.toString(request), host));
        return response.getReplicated();
    }

    public ReplicationStateDto getReplicationState() {
        LOG.info(MessageFormat.format("Getting replication state from {0}", host));
        GetReplicationStateResponse response = stub.getReplicationState(GetReplicationStateRequest.newBuilder().build());
        LOG.info(MessageFormat.format("Got replication state {0} from {1}", response.getLastOrderNum(), host));
        return new ReplicationStateDto(response.getLastOrderNum());
    }

}
