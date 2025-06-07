package kia.example.springbatch.partitioningWithIntegrationKafka;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class WorkerPartitionBatchConfig {
    private final ItemReader workerPartitionItemReader;
    private final ItemProcessor workerPartitionItemProcessor;
    private final ItemWriter workerPartitionItemWriter;

    public WorkerPartitionBatchConfig(ItemReader workerPartitionItemReader,
                                      ItemProcessor workerPartitionItemProcessor,
                                      ItemWriter workerPartitionItemWriter) {
        this.workerPartitionItemReader = workerPartitionItemReader;
        this.workerPartitionItemProcessor = workerPartitionItemProcessor;
        this.workerPartitionItemWriter = workerPartitionItemWriter;
    }

    @Bean
    public Step workerChunkPartitionedStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("workerStepIntegration", jobRepository)
                .<String, String>chunk(2, transactionManager)
                .reader(workerPartitionItemReader)
                .processor(workerPartitionItemProcessor)
                .writer(workerPartitionItemWriter)
                .build();
    }

    @Bean
    public Job workerChunkPartitionedJob(Step workerChunkPartitionedStep, JobRepository jobRepository) {
        return new JobBuilder("workerChunkPartitionedJob", jobRepository)
                .start(workerChunkPartitionedStep)
                .build();
    }
}
