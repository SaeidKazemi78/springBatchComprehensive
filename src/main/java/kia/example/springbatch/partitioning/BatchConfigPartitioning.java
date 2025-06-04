package kia.example.springbatch.partitioning;



import kia.example.springbatch.model.Person;
import kia.example.springbatch.model.PersonAfterProcess;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfigPartitioning {
    private final EntityManagerFactory entityManagerFactory;
    private final ItemProcessor personProcessor;


    public BatchConfigPartitioning(EntityManagerFactory entityManagerFactory,
                       ItemProcessor personProcessor) {
        this.entityManagerFactory = entityManagerFactory;
        this.personProcessor = personProcessor;
    }


    @Bean
    @StepScope
    public JpaPagingItemReader<Person> readerPartitioner(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end) {
        return new JpaPagingItemReaderBuilder<Person>()
                .name("personItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Person p WHERE p.id BETWEEN :start AND :end")
                .parameterValues(Map.of("start", start, "end", end)) // Pass dynamic parameters
                .pageSize(3)
                .build();
    }


    @Bean
    public JpaItemWriter<PersonAfterProcess> writerPartitioner() {
        JpaItemWriter<PersonAfterProcess> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }




    @Bean
    @Qualifier("workerStep")
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager ,JpaPagingItemReader readerPartitioner ) {
        return new StepBuilder("workerStep" ,jobRepository )
                .<Person, PersonAfterProcess>chunk(2 , transactionManager)
                .reader(readerPartitioner)
                .processor(personProcessor)
                .writer(writerPartitioner())
                .build();
    }
    @Bean
    public PartitionHandler partitionHandler(Step workerStep) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(new SimpleAsyncTaskExecutor()); // Parallel execution
        handler.setStep(workerStep); // Worker step
        handler.setGridSize(3); // Number of partitions
        return handler;
    }

    @Bean
    public Step masterStep( JobRepository jobRepository ,@Qualifier("workerStep") Step stepWorker, ColumnRangePartitioner columnRangePartitioner) {
        return new StepBuilder("masterStep" , jobRepository)
                .partitioner("workerStep", columnRangePartitioner)
                .partitionHandler(partitionHandler(stepWorker))
                .build();
    }

    @Bean
    public Job partitionedJob(Step masterStep , JobRepository jobRepository) {
        return new JobBuilder("partitionedJob" , jobRepository)
                .start(masterStep)
                .build();
    }
}