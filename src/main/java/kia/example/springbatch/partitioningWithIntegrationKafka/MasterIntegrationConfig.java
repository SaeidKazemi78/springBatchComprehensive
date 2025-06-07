package kia.example.springbatch.partitioningWithIntegrationKafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MasterIntegrationConfig {

    @Bean
    public MessageChannel toKafka() {

        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow toKafkaFlow(KafkaTemplate kafkaTemplate) {
        return IntegrationFlow.from(toKafka())
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .topic("batch-partitions-kafka"))
                .get();
    }
}
