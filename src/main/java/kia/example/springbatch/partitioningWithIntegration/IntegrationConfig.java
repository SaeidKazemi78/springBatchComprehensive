package kia.example.springbatch.partitioningWithIntegration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
@Configuration
public class IntegrationConfig {
    @Bean
    public MessageChannel requests() {
        return new DirectChannel(); // Channel for sending partition data
    }
    @Bean
    public MessageChannel replies() {
        return new DirectChannel(); // Channel for receiving results
    }
    @ServiceActivator(inputChannel = "requestsChannel")
    public void handleRequests(String partitionData) {
        System.out.println("Worker received partition data: " + partitionData);

        // Simulate processing and send results back to repliesChannel
        System.out.println("Processing partition: " + partitionData);
    }

    @ServiceActivator(inputChannel = "repliesChannel")
    public void handleReplies(String reply) {
        System.out.println("Master received reply: " + reply);
    }
}
