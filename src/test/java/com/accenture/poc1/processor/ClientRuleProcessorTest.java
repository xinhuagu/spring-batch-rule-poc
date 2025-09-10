package com.accenture.poc1.processor;

import com.accenture.poc1.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClientRuleProcessorTest {

    @Autowired
    private ClientRuleProcessor clientRuleProcessor;

    @Test
    void testProcessClient() throws Exception {
        // Create test client
        Client inputClient = new Client(1, "John Doe", 30);

        // Process the client
        Client processedClient = clientRuleProcessor.process(inputClient);

        // Verify transformations
        assertNotNull(processedClient, "Processed client should not be null");
        assertEquals(1, processedClient.getId(), "ID should remain unchanged");
        assertEquals("JOHN DOE", processedClient.getName(), "Name should be uppercase");
        assertEquals(30, processedClient.getAge(), "Age should remain unchanged");
        assertEquals("Adult", processedClient.getAgeCategory(), "Age category should be Adult for age 30");
    }

    @Test
    void testProcessClientWithYoungAge() throws Exception {
        Client inputClient = new Client(2, "Jane Smith", 22);

        Client processedClient = clientRuleProcessor.process(inputClient);

        assertNotNull(processedClient);
        assertEquals("JANE SMITH", processedClient.getName());
        assertEquals("Young", processedClient.getAgeCategory());
    }

    @Test
    void testProcessClientWithSeniorAge() throws Exception {
        Client inputClient = new Client(3, "Bob Johnson", 50);

        Client processedClient = clientRuleProcessor.process(inputClient);

        assertNotNull(processedClient);
        assertEquals("BOB JOHNSON", processedClient.getName());
        assertEquals("Senior", processedClient.getAgeCategory());
    }

    @Test
    void testProcessNullClient() throws Exception {
        Client result = clientRuleProcessor.process(null);
        assertNull(result, "Null input should return null");
    }
}