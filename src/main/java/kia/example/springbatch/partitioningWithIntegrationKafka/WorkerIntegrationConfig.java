package kia.example.springbatch.partitioningWithIntegrationKafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.messaging.MessageChannel;

@Configuration
public class WorkerIntegrationConfig {

    @Bean
    public MessageChannel fromKafka() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow fromKafkaFlow(ConsumerFactory<String, String> consumerFactory) {
        return IntegrationFlow
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, "batch-partitions-kafka"))
                .channel(fromKafka())
                .get();
    }
}
