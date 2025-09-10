package com.accenture.poc1.rule;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RuleEngineTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Test
    void testNameTransformation() {
        String result = ruleEngine.applyRules("name", "John Doe");
        assertEquals("JOHN DOE", result, "Name should be transformed to uppercase");
    }

    @Test
    void testAgeCategorization() {
        // Test Young category
        String youngResult = ruleEngine.applyRules("age", 25);
        assertEquals("Young", youngResult, "Age 25 should be categorized as Young");

        // Test Adult category
        String adultResult = ruleEngine.applyRules("age", 30);
        assertEquals("Adult", adultResult, "Age 30 should be categorized as Adult");

        // Test Senior category
        String seniorResult = ruleEngine.applyRules("age", 45);
        assertEquals("Senior", seniorResult, "Age 45 should be categorized as Senior");
    }

    @Test
    void testEdgeCases() {
        // Test boundary values
        assertEquals("Young", ruleEngine.applyRules("age", 25), "Age 25 should be Young");
        assertEquals("Adult", ruleEngine.applyRules("age", 26), "Age 26 should be Adult");
        assertEquals("Adult", ruleEngine.applyRules("age", 40), "Age 40 should be Adult");
        assertEquals("Senior", ruleEngine.applyRules("age", 41), "Age 41 should be Senior");
    }

    @Test
    void testNullValues() {
        String result = ruleEngine.applyRules("name", null);
        assertNull(result, "Null input should return null");
    }

    @Test
    void testUnknownField() {
        String result = ruleEngine.applyRules("unknownField", "test");
        assertEquals("test", result, "Unknown field should return original value");
    }
}