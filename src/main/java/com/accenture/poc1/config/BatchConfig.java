package com.accenture.poc1.config;

import com.accenture.poc1.model.Client;
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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

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

    @Bean
    public Job clientToCsvJob() {
        return new JobBuilder("clientToCsvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(exportClientToCsvStep())
                .build();
    }

    @Bean
    public Step exportClientToCsvStep() {
        return new StepBuilder("exportClientToCsvStep", jobRepository)
                .<Client, Client>chunk(10, transactionManager)
                .reader(clientItemReader())
                .writer(clientCsvItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Client> clientItemReader() {
        return new JdbcCursorItemReaderBuilder<Client>()
                .name("clientItemReader")
                .dataSource(dataSource)
                .sql("SELECT id, name, age FROM client ORDER BY id")
                .rowMapper(new BeanPropertyRowMapper<>(Client.class))
                .build();
    }

    @Bean
    public FlatFileItemWriter<Client> clientCsvItemWriter() {
        BeanWrapperFieldExtractor<Client> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "name", "age"});

        DelimitedLineAggregator<Client> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<Client>()
                .name("clientCsvItemWriter")
                .resource(new FileSystemResource("clients_export.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("id,name,age"))
                .build();
    }
}