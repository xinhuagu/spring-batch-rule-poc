package com.accenture.poc1.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job sampleJob() {
        return new JobBuilder("sampleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(sampleStep())
                .build();
    }

    @Bean
    public Step sampleStep() {
        return new StepBuilder("sampleStep", jobRepository)
                .tasklet(sampleTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet sampleTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Executing sample tasklet");
            log.info("Job Name: {}", chunkContext.getStepContext().getJobName());
            log.info("Step Name: {}", chunkContext.getStepContext().getStepName());
            log.info("Job Execution ID: {}", chunkContext.getStepContext().getStepExecution().getJobExecutionId());
            log.info("Sample tasklet completed successfully");
            return RepeatStatus.FINISHED;
        };
    }
}