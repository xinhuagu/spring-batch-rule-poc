package com.accenture.poc1;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClientToCsvManualTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("clientToCsvJob")
    private Job clientToCsvJob;

    @Test
    void testClientToCsvJobManual() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(clientToCsvJob, jobParameters);
        
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        // Verify CSV file was created
        File csvFile = new File("clients_export.csv");
        assertTrue(csvFile.exists(), "CSV file should be created");
        
        // Verify CSV content has header and data
        String content = Files.readString(Paths.get("clients_export.csv"));
        assertTrue(content.startsWith("id,name,age"), "CSV should have header row");
        assertTrue(content.contains("John Doe"), "CSV should contain test data");
        
        // Clean up
        csvFile.delete();
    }
}