package kia.example.springbatch.simple;


import jakarta.persistence.EntityManagerFactory;
import kia.example.springbatch.model.Person;
import kia.example.springbatch.model.PersonAfterProcess;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfigSimple {

    private final EntityManagerFactory entityManagerFactory;
    private final ItemProcessor personProcessor;
    private final SampleTasklet tasklet;

    public BatchConfigSimple(EntityManagerFactory entityManagerFactory,
                             ItemProcessor personProcessor,
                             SampleTasklet tasklet) {
        this.entityManagerFactory = entityManagerFactory;
        this.personProcessor = personProcessor;
        this.tasklet = tasklet;
    }

    @Bean
    public JpaPagingItemReader<Person> reader() {
        return new JpaPagingItemReaderBuilder<Person>()
                .name("personItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Person p")
                .pageSize(10)
                .build();
    }


    @Bean
    public JpaItemWriter<PersonAfterProcess> writer() {
        JpaItemWriter<PersonAfterProcess> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job processPersonJob(JobRepository jobRepository, @Qualifier("stepPersonChunk") Step step) {
        return new JobBuilder("processPersonJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step stepPersonChunk(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepPersonChunk", jobRepository)
                .<Person, PersonAfterProcess>chunk(10, transactionManager)
                .reader(reader())
                .processor(personProcessor)
                .writer(writer())
                .build();
    }
    @Bean
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStep" , jobRepository)
                .tasklet(tasklet , transactionManager )
                .build();
    }

}
