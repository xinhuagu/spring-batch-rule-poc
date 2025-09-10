package com.accenture.poc1;

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
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class ClientToCsvJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("clientToCsvJob")
    private Job clientToCsvJob;

    @Test
    void testClientToCsvJob() throws Exception {
        jobLauncherTestUtils.setJob(clientToCsvJob);
        
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        // Verify CSV file was created
        File csvFile = new File("clients_export.csv");
        assertTrue(csvFile.exists(), "CSV file should be created");
        
        // Verify CSV content has header with ageCategory
        String content = Files.readString(Paths.get("clients_export.csv"));
        assertTrue(content.startsWith("id,name,age,ageCategory"), "CSV should have header row with ageCategory");
    }
}