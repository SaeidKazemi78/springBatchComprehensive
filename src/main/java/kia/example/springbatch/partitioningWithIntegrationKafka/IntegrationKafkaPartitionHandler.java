package kia.example.springbatch.partitioningWithIntegrationKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class IntegrationKafkaPartitionHandler implements PartitionHandler {

    private final MessageChannel messageChannel;

    public IntegrationKafkaPartitionHandler(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public Collection<StepExecution> handle(StepExecutionSplitter splitter, StepExecution stepExecution) throws Exception {
        var gridSize = 3 ;
        Set<StepExecution> partitions = splitter.split(stepExecution, gridSize);

        for ( StepExecution partition : partitions ) {
            Message<String> message = MessageBuilder
                    .withPayload(partition.getExecutionContext().toString())
                    .build();
            messageChannel.send(message);
        }
        return Collections.emptyList();
    }
}
