package kia.example.springbatch.partitioningWithIntegrationKafka;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
@StepScope
public class WorkerPartitionItemReader implements ItemReader<String> {
    @Value("#{jobParameters['partitionNumber']}")
    private Integer partitionNumber;

    private int count = 0;

    @Override
    public String read() {
        if (count < 5) {
            return "Item " + count++ + " from partition " + partitionNumber;
        }
        return null;
    }
}
