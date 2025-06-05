package kia.example.springbatch.introIntegration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

@Configuration
@EnableIntegration // Enable Spring Integration infrastructure without this code get error Dispatcher has no subscribers
public class IntegrationFlowExample {
    @Bean
    public MessageChannel inputChannel3() {
        return new DirectChannel();
    }
    @Bean
    public MessageChannel outputChannel3() {
        return new DirectChannel();
    }
    @Bean
    public IntegrationFlow flow() {
        return IntegrationFlow.from(inputChannel3())
                .transform(String.class, String::toUpperCase)//you make transformer bean and pass here with your custom logic
                .channel(outputChannel3())
                .get();
    }
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(IntegrationFlowExample.class);
        MessageChannel inputChannel = context.getBean("inputChannel3", MessageChannel.class);
        MessageChannel outputChannel = context.getBean("outputChannel3", MessageChannel.class);
        ((DirectChannel) outputChannel).subscribe(message -> System.out.println("Transformed message: " + message.getPayload()));
        // Send a message
        inputChannel.send(new GenericMessage<>("hello, integration flow!"));
        context.close();
    }
}
