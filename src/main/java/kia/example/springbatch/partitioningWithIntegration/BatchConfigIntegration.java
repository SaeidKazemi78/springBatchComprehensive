package kia.example.springbatch.partitioningWithIntegration;

import jakarta.persistence.EntityManagerFactory;
import kia.example.springbatch.model.Person;
import kia.example.springbatch.model.PersonAfterProcess;
import kia.example.springbatch.partitioning.ColumnRangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfigIntegration {

    private final MessageChannel requestsChannel;
    private final MessageChannel repliesChannel;
    private final EntityManagerFactory entityManagerFactory;
    private final ItemProcessor<Person, PersonAfterProcess> personProcessor;

    public BatchConfigIntegration(@Qualifier("requests") MessageChannel requestsChannel, @Qualifier("replies") MessageChannel repliesChannel, EntityManagerFactory entityManagerFactory, ItemProcessor<Person, PersonAfterProcess> personProcessor) {
        this.requestsChannel = requestsChannel;
        this.repliesChannel = repliesChannel;
        this.entityManagerFactory = entityManagerFactory;
        this.personProcessor = personProcessor;
    }

    // ** PARTITIONING LOGIC **

    @Bean
    public ColumnRangePartitioner partitionerIntegration() {
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandlerIntegration() {
        MessagingTemplate messagingTemplate = new MessagingTemplate();
        messagingTemplate.setDefaultChannel(requestsChannel);

        return (splitter, stepExecution) -> {
            var partitions = splitter.split(stepExecution, 3); // Create at least 3 partitions

            for (var partition : partitions) {
                var start = partition.getExecutionContext().getInt("start");
                var end = partition.getExecutionContext().getInt("end");
                var metadata = Map.of("start", start, "end", end);

                messagingTemplate.send(MessageBuilder.withPayload(metadata).build());
                System.out.println("Master sent metadata to requestsChannel: " + metadata);
            }

            return partitions;
        };
    }

    @Bean
    public Step masterStepIntegration(JobRepository jobRepository, @Qualifier("partitionerIntegration") ColumnRangePartitioner partitioner) {
        return new StepBuilder("masterStepIntegration", jobRepository)
                .partitioner("workerStepIntegration", partitioner)
                .partitionHandler(partitionHandlerIntegration())
                .build();
    }

    // ** WORKER LOGIC **

    @Bean
    @StepScope
    public JpaPagingItemReader<Person> readerPartitionerIntegration(@Value("#{stepExecutionContext['start']}") Integer start, @Value("#{stepExecutionContext['end']}") Integer end) {
        return new JpaPagingItemReaderBuilder<Person>().name("personItemReader").entityManagerFactory(entityManagerFactory).queryString("SELECT p FROM Person p WHERE p.id BETWEEN :start AND :end").parameterValues(Map.of("start", start, "end", end)).pageSize(3).build();
    }

    @Bean
    public JpaItemWriter<PersonAfterProcess> writerPartitionerIntegration() {
        JpaItemWriter<PersonAfterProcess> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    @Qualifier("workerStepIntegration")
    public Step workerStepIntegration(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      @Qualifier("readerPartitionerIntegration") JpaPagingItemReader<Person> readerPartitioner) {
        return new StepBuilder("workerStepIntegration", jobRepository)
                .<Person, PersonAfterProcess>chunk(2, transactionManager)
                .reader(readerPartitioner)
                .processor(personProcessor).
                writer(writerPartitionerIntegration())
                .build();
    }

    @ServiceActivator(inputChannel = "requests")
    public void workerServiceActivator(String partitionData) {
        System.out.println("Worker received partition data: " + partitionData);

        // Simulate reading data and sending processed data to repliesChannel
        String processedData = "Processed partition: " + partitionData;
        repliesChannel.send(MessageBuilder.withPayload(processedData).build());
        System.out.println("Worker sent processed data to repliesChannel: " + processedData);
    }
    @ServiceActivator(inputChannel = "replies")
    public void handleReplies(String processedData) {
        System.out.println("Master received processed data from repliesChannel: " + processedData);
        // You can process the data further here if needed
    }

    // ** JOB CONFIGURATION **

    @Bean
    public Job partitionedJobIntegration(Step masterStepIntegration, JobRepository jobRepository) {
        return new JobBuilder("partitionedJobIntegration", jobRepository)
                .start(masterStepIntegration)
                .build();
    }
}