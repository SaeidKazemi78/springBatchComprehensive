package kia.example.springbatch.introIntegration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
@Configuration
public class DirectChannelExample {
    @Bean
    public MessageChannel channel() {
        return new DirectChannel();
    }
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DirectChannelExample.class);
        MessageChannel channel = context.getBean("channel", MessageChannel.class);
        // Define a message handler
        MessageHandler handler = message -> System.out.println("Received message: " + message.getPayload());
        ((DirectChannel) channel).subscribe(handler);
        // Send a message
        channel.send(new GenericMessage<>("Hello, DirectChannel!"));
        context.close();
    }
}
