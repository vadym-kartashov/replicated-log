package org.vkartashov.log.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vkartashov.log.service.replication.MessageServiceClient;

import java.util.List;
import java.util.stream.Stream;

@Configuration
public class AppConfig {

    @Value("${replication-log.replicas}")
    private String replicasList;

    @Bean
    @ConditionalOnExpression("!${replication-log.is-replica}")
    public List<MessageServiceClient> replicas(){
        var replicaHosts = replicasList.split(",");
        return Stream.of(replicaHosts)
                .map(this::messageServiceClient)
                .toList();
    }

    private MessageServiceClient messageServiceClient(String url) {
        return new MessageServiceClient(url);
    }

}
