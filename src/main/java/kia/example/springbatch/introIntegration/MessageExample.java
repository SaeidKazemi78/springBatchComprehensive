package kia.example.springbatch.introIntegration;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
public class MessageExample {
    public static void main(String[] args) {
        // Create a message with a payload and headers
        Message<String> message = MessageBuilder
                .withPayload("Hello, Spring Integration!")
                .setHeader("key", "value")
                .setHeader("part", "1")//example of key value
                .build();
        System.out.println("Payload: " + message.getPayload());
        System.out.println("Headers: " + message.getHeaders());
    }
}