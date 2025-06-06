package kia.example.springbatch.partitioningWithKafka;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class BatchConfigPartitioningKafka {

    private final EntityManagerFactory entityManagerFactory;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public BatchConfigPartitioningKafka(EntityManagerFactory entityManagerFactory,
                                        KafkaTemplate<String, String> kafkaTemplate) {
        this.entityManagerFactory = entityManagerFactory;
        this.kafkaTemplate = kafkaTemplate;
    }



    @Bean
    public Step masterStepConfigPartitioningKafka(JobRepository jobRepository) {
        return new StepBuilder("masterStepConfigPartitioningKafka" , jobRepository)
                .partitioner("workerStepConfigPartitioningKafka", partitionerWithKafka())
                .partitionHandler(new KafkaPartitionHandler(kafkaTemplate, "batch-partitions"))
                .build();
    }

    @Bean
    public Partitioner partitionerWithKafka() {
        return (gridSize) -> {
            Map<String, ExecutionContext> map = new HashMap<>();
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                context.putInt("partitionNumber", i);
                map.put("partition" + i, context);
            }
            return map;
        };
    }

    @Bean
    public Job partitionJobWithKafka(Step masterStepConfigPartitioningKafka , JobRepository jobRepository) {
        return new JobBuilder("partitionJobWithKafka" , jobRepository)
                .start(masterStepConfigPartitioningKafka)
                .build();
    }
}
