package kia.example.springbatch.api;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job processPersonJob;
    private final Job partitionedJob;

    public JobController(JobLauncher jobLauncher,
                         Job processPersonJob,
                         Job partitionedJob) {
        this.jobLauncher = jobLauncher;
        this.processPersonJob = processPersonJob;
        this.partitionedJob = partitionedJob;
    }

    @GetMapping("simple/run")
    public ResponseEntity<String> runJob() {
        try {
            // Add unique parameters to avoid job execution conflicts
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(processPersonJob, jobParameters);
            return ResponseEntity.ok("Job started successfully with status: " + execution.getStatus());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Jobs failed to start: " + e.getMessage());
        }
    }
    @GetMapping("partitioning/run")
    public ResponseEntity<String> runPartitionedJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // Unique parameter
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(partitionedJob, jobParameters);
            return ResponseEntity.ok("Job started successfully with status: " + execution.getStatus());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to start job: " + e.getMessage());
        }
    }
}
