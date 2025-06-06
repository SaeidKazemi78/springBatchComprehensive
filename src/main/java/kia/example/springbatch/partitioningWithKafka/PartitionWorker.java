package kia.example.springbatch.partitioningWithKafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PartitionWorker {
    @KafkaListener(topics = "batch-partitions", groupId = "batch-workers")
    public void listen(String partitionData) {
        // Parse partitionData back to ExecutionContext
        // Execute the partitioned step logic here
        System.out.println("Received partition: " + partitionData);
        // Run the step using this partition data
    }
}
