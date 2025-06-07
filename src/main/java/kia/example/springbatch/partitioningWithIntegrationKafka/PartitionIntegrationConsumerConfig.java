package kia.example.springbatch.partitioningWithIntegrationKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class PartitionIntegrationConsumerConfig {

    private final JobLauncher jobLauncher;

    private final Job workerChunkPartitionedJob;

    public PartitionIntegrationConsumerConfig(JobLauncher jobLauncher, Job workerChunkPartitionedJob) {
        this.jobLauncher = jobLauncher;
        this.workerChunkPartitionedJob = workerChunkPartitionedJob;
    }

    @ServiceActivator(inputChannel = "fromKafka")
    public void handlePartition(Message<String> message) throws Exception {
        String partitionData = message.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();

        JobParametersBuilder builder = new JobParametersBuilder();

        builder.addString("meta", partitionData);

        builder.addLong("run.id", System.currentTimeMillis());

        jobLauncher.run(workerChunkPartitionedJob, builder.toJobParameters());
    }
}
