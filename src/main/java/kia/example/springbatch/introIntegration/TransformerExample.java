package kia.example.springbatch.introIntegration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

@Configuration
@EnableIntegration // Enable Spring Integration infrastructure without this code get error Dispatcher has no subscribers
public class TransformerExample {
    @Bean
    public MessageChannel inputChannel2() {
        return new DirectChannel();
    }
    @Bean
    public MessageChannel outputChannel2() {
        return new DirectChannel();
    }
    @Transformer(inputChannel = "inputChannel2", outputChannel = "outputChannel2")
    public String transformMessage(String message) {
        return message.toUpperCase() + "add some logic here";
    }
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TransformerExample.class);
        MessageChannel inputChannel = context.getBean("inputChannel2", MessageChannel.class);
        MessageChannel outputChannel = context.getBean("outputChannel2", MessageChannel.class);
        ((DirectChannel) outputChannel).subscribe(message -> System.out.println("Transformed message: " + message.getPayload()));
        // Send a message
        inputChannel.send(new GenericMessage<>("hello, transformer!"));
        context.close();
    }
}
