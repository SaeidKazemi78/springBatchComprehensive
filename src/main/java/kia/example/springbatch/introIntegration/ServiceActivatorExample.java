package kia.example.springbatch.introIntegration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

@Configuration
@EnableIntegration // Enable Spring Integration infrastructure without this code get error Dispatcher has no subscribers
public class ServiceActivatorExample {

    @Bean
    public MessageChannel inputChannel1() {
        return new DirectChannel();
    }

    @ServiceActivator(inputChannel = "inputChannel1")
    public void handleMessage(String message) {
        System.out.println("Processed message: " + message.toUpperCase() + "This important here to infra");
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceActivatorExample.class);

        // Get the channel bean
        MessageChannel channel = context.getBean("inputChannel1", MessageChannel.class);

        // Send a message
        channel.send(new GenericMessage<>("hello, service activator!"));

        context.close();
    }
}
