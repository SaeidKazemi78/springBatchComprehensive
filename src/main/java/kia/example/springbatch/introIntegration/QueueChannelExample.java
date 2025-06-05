package kia.example.springbatch.introIntegration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;

import org.springframework.messaging.support.GenericMessage;
public class QueueChannelExample {
    public static void main(String[] args) {
        QueueChannel channel = new QueueChannel();
        // Send messages to the channel
        channel.send(new GenericMessage<>("Message 1"));
        channel.send(new GenericMessage<>("Message 2"));
        channel.send(new GenericMessage<>("Message 3"));
        channel.send(new GenericMessage<>("Message 4"));
        channel.send(new GenericMessage<>("Message 5"));
        channel.send(new GenericMessage<>("Message 6"));
        // Receive messages from the channel
        Message<?> message1 = channel.receive();
        Message<?> message2 = channel.receive();
        Message<?> message3 = channel.receive();
        Message<?> message4 = channel.receive();
        Message<?> message5 = channel.receive();
        Message<?> message6 = channel.receive();
        System.out.println("Received: " + message1.getPayload());
        System.out.println("Received: " + message2.getPayload());
        System.out.println("Received: " + message3.getPayload());
        System.out.println("Received: " + message4.getPayload());
        System.out.println("Received: " + message5.getPayload());
        System.out.println("Received: " + message6.getPayload());
    }
}
