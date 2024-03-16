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
        int maxRetries = 10;
        int retryCount = 0;
        int baseDelay = 1;  // Delay in seconds
        int backoffFactor = 2; // How much delay increases on each retry

        while (retryCount < maxRetries) {
            try {
                LOG.info(MessageFormat.format("Replicating {0} to {1}", LogProtoUtil.toString(request), host));
                ReplicateResponse response = stub.replicate(
                        ReplicateRequest.newBuilder()
                                .setMessage(request.getMessage())
                                .setOrderNum(request.getOrderNum())
                                .build()
                );
                LOG.info(MessageFormat.format("Replicated {0} to {1}", LogProtoUtil.toString(request), host));
                return response.getReplicated();

            } catch (Exception e) {
                LOG.warn("Replication failed. Retrying..." + e.getMessage());
                int delay = baseDelay * (int) Math.pow(backoffFactor, retryCount);
                try {
                    Thread.sleep(delay * 1000); // Sleep is in milliseconds
                } catch (InterruptedException ie) {
                    // Handle the interrupted exception if needed
                }
                retryCount++;
            }
        }

        // If all retries fail
        LOG.error("Replication failed after all retries.");
        return false;
    }

    public ReplicationStateDto getReplicationState() {
        LOG.info(MessageFormat.format("Getting replication state from {0}", host));
        GetReplicationStateResponse response = stub.getReplicationState(GetReplicationStateRequest.newBuilder().build());
        LOG.info(MessageFormat.format("Got replication state {0} from {1}", response.getLastOrderNum(), host));
        return new ReplicationStateDto(response.getLastOrderNum());
    }

}
