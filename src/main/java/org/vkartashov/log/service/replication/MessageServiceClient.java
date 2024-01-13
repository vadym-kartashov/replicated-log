package org.vkartashov.log.service.replication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vkartashov.log.util.LogProtoUtil;

import java.text.MessageFormat;

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
        LOG.info(MessageFormat.format("Replicating {0} to {1}", LogProtoUtil.toString(request), host));
        ReplicateResponse response = stub.replicate(
                ReplicateRequest.newBuilder()
                        .setMessage(request.getMessage())
                        .build()
        );
        LOG.info(MessageFormat.format("Replicated {0} to {1}", LogProtoUtil.toString(request), host));
        return response.getReplicated();
    }

}
