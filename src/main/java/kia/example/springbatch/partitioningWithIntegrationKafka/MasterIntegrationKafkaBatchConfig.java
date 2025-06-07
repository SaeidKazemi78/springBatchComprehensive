package kia.example.springbatch.partitioningWithIntegrationKafka;

import jakarta.persistence.EntityManagerFactory;
import kia.example.springbatch.model.Person;
import kia.example.springbatch.model.PersonAfterProcess;
import kia.example.springbatch.partitioning.ColumnRangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class MasterIntegrationKafkaBatchConfig {

    private final EntityManagerFactory entityManagerFactory;

    private final MessageChannel toKafka;

    public MasterIntegrationKafkaBatchConfig(EntityManagerFactory entityManagerFactory,
                                             MessageChannel toKafka) {
        this.entityManagerFactory = entityManagerFactory;
        this.toKafka = toKafka;
    }

    @Bean
    public Step masterIntegrationKafkaStep(JobRepository jobRepository) {
        return new StepBuilder("masterIntegrationKafkaStep", jobRepository)
                .partitioner("workerIntegrationKafkaStep", partitionerStepIntegrationKafka())
                .partitionHandler(new IntegrationKafkaPartitionHandler(toKafka))
                .build();
    }

    @Bean
    public Partitioner partitionerStepIntegrationKafka() {
        return gridSize -> {
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
    public Job partitionedJobIntegrationKafka(Step masterIntegrationKafkaStep, JobRepository jobRepository) {
        return new JobBuilder("partitionedJobIntegrationKafka", jobRepository)
                .start(masterIntegrationKafkaStep)
                .build();
    }
}
