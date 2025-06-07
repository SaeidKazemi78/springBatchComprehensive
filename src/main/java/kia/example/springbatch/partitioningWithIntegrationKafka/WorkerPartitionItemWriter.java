package kia.example.springbatch.partitioningWithIntegrationKafka;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;



@Component
public class WorkerPartitionItemWriter implements ItemWriter<String> {
    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        chunk.forEach(System.out::println);

    }
}
