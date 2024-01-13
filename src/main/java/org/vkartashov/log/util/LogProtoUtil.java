package org.vkartashov.log.util;

import org.vkartashov.log.service.replication.ReplicateRequest;

public class LogProtoUtil {

    public static String toString(ReplicateRequest request) {
        return request.getMessage() + " " + request.getOrderNum();
    }

}