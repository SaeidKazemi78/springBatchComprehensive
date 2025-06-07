package kia.example.springbatch.partitioningWithIntegrationKafka;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class WorkerPartitionItemProcessor implements ItemProcessor<String, String> {
    @Override
    public String process(String item) {
        return item.toUpperCase();
    }
}
