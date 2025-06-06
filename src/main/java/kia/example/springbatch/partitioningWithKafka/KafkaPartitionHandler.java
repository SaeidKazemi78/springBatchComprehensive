package kia.example.springbatch.partitioningWithKafka;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class KafkaPartitionHandler implements PartitionHandler {
    private final KafkaTemplate kafkaTemplate;
    private final String topic;

    public KafkaPartitionHandler(KafkaTemplate kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public Collection<StepExecution> handle(StepExecutionSplitter splitter, StepExecution stepExecution) throws Exception {
        var gridSize = 3 ;//you manage by application property or by custom logic
        Set<StepExecution> partitions = splitter.split(stepExecution, gridSize);
        for (StepExecution entry : partitions) {
            String partitionData = entry.toString(); // You may want to use JSON serialization
            kafkaTemplate.send(topic, partitionData);
        }
        // Return empty, since the actual StepExecutions are handled by workers.
        return Collections.emptyList();
    }
}
