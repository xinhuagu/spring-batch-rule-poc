package com.accenture.poc1.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.batch.jdbc.initialize-schema=always",
    "spring.batch.jdbc.isolation-level-for-create=default",
    "spring.sql.init.mode=always"
})
@SpringBatchTest
@SpringJUnitConfig
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.batch.jdbc.initialize-schema=always",
    "spring.batch.jdbc.isolation-level-for-create=default",
    "spring.sql.init.mode=always",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL"
})
@EnableAutoConfiguration
@Import(TestBatchConfiguration.class)
class BatchConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    @Qualifier("clientToCsvJob")
    private Job clientToCsvJob;

    @Test
    void testClientToCsvJob() throws Exception {
        jobLauncherTestUtils.setJob(clientToCsvJob);
        
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        assertEquals("clientToCsvJob", jobExecution.getJobInstance().getJobName());
    }

    @Test
    void testExportClientToCsvStep() throws Exception {
        jobLauncherTestUtils.setJob(clientToCsvJob);
        
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("exportClientToCsvStep");
        
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        assertEquals(1, jobExecution.getStepExecutions().size());
    }
}