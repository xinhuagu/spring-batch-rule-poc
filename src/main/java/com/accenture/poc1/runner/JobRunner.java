package com.accenture.poc1.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    
    @Qualifier("clientToCsvJob")
    private final Job clientToCsvJob;

    @Override
    public void run(String... args) throws Exception {
        // Check if we should run the job based on command line arguments
        boolean runClientToCsvJob = false;
        for (String arg : args) {
            if (arg.contains("clientToCsv") || arg.contains("--job=clientToCsv")) {
                runClientToCsvJob = true;
                break;
            }
        }

        if (runClientToCsvJob) {
            log.info("Starting clientToCsvJob...");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(clientToCsvJob, jobParameters);
            log.info("Job Status: {}", jobExecution.getStatus());
            log.info("Job Exit Status: {}", jobExecution.getExitStatus().getExitCode());
        }
    }
}