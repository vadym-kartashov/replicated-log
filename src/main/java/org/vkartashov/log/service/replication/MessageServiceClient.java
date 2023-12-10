package org.vkartashov.log.service.replication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(MessageServiceClient.class);

    private final LogReplicationServiceGrpc.LogReplicationServiceBlockingStub stub;
    @Getter
    private final String host;


    public MessageServiceClient(String host) {
        this.host = host;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build();
        stub = LogReplicationServiceGrpc.newBlockingStub(channel);
    }

    public boolean replicate(ReplicateRequest request) {
        LOG.info("Replicating " + request + " to " + host);
        ReplicateResponse response = stub.replicate(
                ReplicateRequest.newBuilder()
                        .setMessage(request.getMessage())
                        .build()
        );
        LOG.info("Replicated " + request + " to " + host);
        return response.getReplicated();
    }

}
