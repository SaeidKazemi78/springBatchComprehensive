package kia.example.springbatch.partitioningWithIntegration;

import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.batch.core.StepExecution;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collection;

public class CustomPartitionHandler implements PartitionHandler {

    private final MessagingTemplate messagingTemplate;
    private final MessageChannel requestsChannel;


    public CustomPartitionHandler(MessagingTemplate messagingTemplate,
                                  MessageChannel requestsChannel) {
        this.messagingTemplate = messagingTemplate;
        this.requestsChannel = requestsChannel;
    }


    @Override
    public Collection<StepExecution> handle(StepExecutionSplitter stepSplitter, StepExecution masterStepExecution) throws Exception {
        //can define grid siz in application properties
        Collection<StepExecution> partitions = stepSplitter.split(masterStepExecution, 3);

        for (StepExecution partition : partitions) {
            Message<String> message = MessageBuilder
                    .withPayload("Partition metadata: " + partition.getExecutionContext().toString())
                    .build();

            messagingTemplate.send(requestsChannel, message);
            System.out.println("Master sent partition data to requestsChannel: " + message.getPayload());
        }

        return partitions;
    }
}
