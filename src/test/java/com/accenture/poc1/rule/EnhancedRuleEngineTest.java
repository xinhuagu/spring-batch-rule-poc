package com.accenture.poc1.rule;

import com.accenture.poc1.rule.EnhancedRuleEngine.RuleMetrics;
import com.accenture.poc1.rule.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnhancedRuleEngineTest {

    private EnhancedRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new EnhancedRuleEngine();
    }

    @Test
    void testPrioritySystem() {
        // Test that rules are applied in priority order
        // MASK rules (priority 10-20) should be applied before TRANSFORM (priority 200)
        
        String result = ruleEngine.applyRules("name", "Michael Chen");
        assertEquals("*********", result, "MASK rule should be applied first due to lower priority");
        
        result = ruleEngine.applyRules("name", "David Thomas");
        assertEquals("*******", result, "Specific MASK rule should be applied");
        
        result = ruleEngine.applyRules("name", "John Doe");
        assertEquals("JOHN DOE", result, "TRANSFORM rule should be applied when no MASK matches");
    }

    @Test
    void testValidateRuleType() {
        // Test email validation
        String validEmail = ruleEngine.applyRules("email", "test@example.com");
        assertEquals("test@example.com", validEmail, "Valid email should pass through");
        
        String invalidEmail = ruleEngine.applyRules("email", "invalid-email");
        assertNull(invalidEmail, "Invalid email should be rejected");
        
        // Test phone validation with default value
        String validPhone = ruleEngine.applyRules("phone", "555-123-4567");
        assertEquals("555-123-4567", validPhone, "Valid phone should pass through");
        
        String invalidPhone = ruleEngine.applyRules("phone", "invalid");
        assertEquals("000-000-0000", invalidPhone, "Invalid phone should use default value");
    }

    @Test
    void testFormatRuleType() {
        // Test phone formatting
        String formattedPhone = ruleEngine.applyRules("phone", "5551234567");
        assertEquals("(555) 123-4567", formattedPhone, "Phone should be formatted");
        
        // Test date formatting
        String formattedDate = ruleEngine.applyRules("birthDate", "2000-01-15");
        assertEquals("01/15/2000", formattedDate, "Date should be formatted to MM/dd/yyyy");
    }

    @Test
    void testReplaceRuleType() {
        // Test profanity replacement
        String cleanText = ruleEngine.applyRules("comments", "This contains badword1 in text");
        assertEquals("This contains *** in text", cleanText, "Profanity should be replaced");
        
        String normalText = ruleEngine.applyRules("comments", "This is clean text");
        assertEquals("This is clean text", normalText, "Clean text should remain unchanged");
    }

    @Test
    void testPartialMasking() {
        // Test SSN partial masking
        String maskedSSN = ruleEngine.applyRules("ssn", "123-45-6789");
        assertEquals("XXX-XX-6789", maskedSSN, "SSN should be partially masked showing last 4");
    }

    @Test
    void testMaskingTypes() {
        // Test different masking types
        String fullMask = ruleEngine.applyRules("name", "Chen Wei");
        assertEquals("*********", fullMask, "Full mask should replace entire value");
        
        String partialMaskSSN = ruleEngine.applyRules("ssn", "987-65-4321");
        assertEquals("XXX-XX-4321", partialMaskSSN, "Partial mask should preserve format");
    }

    @Test
    void testCategorizeRule() {
        // Test age categorization
        assertEquals("Young", ruleEngine.applyRules("age", "20"));
        assertEquals("Young", ruleEngine.applyRules("age", "25"));
        assertEquals("Adult", ruleEngine.applyRules("age", "26"));
        assertEquals("Adult", ruleEngine.applyRules("age", "35"));
        assertEquals("Adult", ruleEngine.applyRules("age", "40"));
        assertEquals("Senior", ruleEngine.applyRules("age", "41"));
        assertEquals("Senior", ruleEngine.applyRules("age", "65"));
    }

    @Test
    void testTransformRule() {
        // Test various transform actions
        String uppercase = ruleEngine.applyRules("name", "john doe");
        assertEquals("JOHN DOE", uppercase, "Should transform to uppercase");
    }

    @Test
    void testRuleMetrics() {
        // Execute some rules
        ruleEngine.applyRules("name", "Test Name");
        ruleEngine.applyRules("age", "30");
        ruleEngine.applyRules("email", "test@example.com");
        
        // Get metrics
        Map<String, RuleMetrics> metrics = ruleEngine.getRuleMetrics();
        assertNotNull(metrics, "Metrics should not be null");
        
        // Check that metrics are being tracked
        assertTrue(metrics.size() > 0, "Should have metrics for executed rules");
        
        // Verify metrics structure
        for (RuleMetrics metric : metrics.values()) {
            assertNotNull(metric.getRuleId(), "Rule ID should not be null");
            assertTrue(metric.getExecutionCount() >= 0, "Execution count should be non-negative");
            assertTrue(metric.getTotalExecutionTime() >= 0, "Total time should be non-negative");
            assertTrue(metric.getAverageExecutionTime() >= 0, "Average time should be non-negative");
        }
    }

    @Test
    void testGetRulesForField() {
        List<Rule> nameRules = ruleEngine.getRulesForField("name");
        assertNotNull(nameRules, "Should return rules for name field");
        assertTrue(nameRules.size() > 0, "Should have rules for name field");
        
        List<Rule> ageRules = ruleEngine.getRulesForField("age");
        assertNotNull(ageRules, "Should return rules for age field");
        assertEquals(3, ageRules.size(), "Should have 3 age categorization rules");
        
        List<Rule> unknownRules = ruleEngine.getRulesForField("unknown");
        assertNotNull(unknownRules, "Should return empty list for unknown field");
        assertEquals(0, unknownRules.size(), "Should have no rules for unknown field");
    }

    @Test
    void testReloadRules() {
        // Get initial rule count
        List<Rule> initialNameRules = ruleEngine.getRulesForField("name");
        int initialCount = initialNameRules.size();
        
        // Reload rules
        ruleEngine.reloadRules();
        
        // Verify rules are still loaded
        List<Rule> reloadedNameRules = ruleEngine.getRulesForField("name");
        assertEquals(initialCount, reloadedNameRules.size(), "Should have same number of rules after reload");
    }

    @Test
    void testNullHandling() {
        // Test null value handling
        assertNull(ruleEngine.applyRules("name", null), "Should return null for null input");
        assertNull(ruleEngine.applyRules("age", null), "Should return null for null input");
        
        // Test with empty string
        String emptyResult = ruleEngine.applyRules("name", "");
        assertEquals("", emptyResult, "Should handle empty string");
    }

    @Test
    void testCaseSensitivity() {
        // Test case-insensitive matching for MASK rules
        assertEquals("*********", ruleEngine.applyRules("name", "chen"));
        assertEquals("*********", ruleEngine.applyRules("name", "CHEN"));
        assertEquals("*********", ruleEngine.applyRules("name", "Chen"));
        assertEquals("*********", ruleEngine.applyRules("name", "cheng")); // Contains "chen"
        
        // Test exact match case-insensitive
        assertEquals("*******", ruleEngine.applyRules("name", "david thomas"));
        assertEquals("*******", ruleEngine.applyRules("name", "DAVID THOMAS"));
        assertEquals("*******", ruleEngine.applyRules("name", "David Thomas"));
    }

    @Test
    void testRuleConditionEvaluation() {
        // Create a test rule condition
        Rule.RuleCondition condition = new Rule.RuleCondition();
        
        // Test NOT_NULL operator
        condition.setOperator("NOT_NULL");
        assertTrue(condition.evaluate("test"));
        assertFalse(condition.evaluate(""));
        assertFalse(condition.evaluate(null));
        
        // Test EQUALS operator
        condition.setOperator("EQUALS");
        condition.setValue("test");
        condition.setCaseSensitive(true);
        assertTrue(condition.evaluate("test"));
        assertFalse(condition.evaluate("TEST"));
        
        condition.setCaseSensitive(false);
        assertTrue(condition.evaluate("test"));
        assertTrue(condition.evaluate("TEST"));
        
        // Test CONTAINS operator
        condition.setOperator("CONTAINS");
        condition.setValue("abc");
        condition.setCaseSensitive(false);
        assertTrue(condition.evaluate("xabcy"));
        assertTrue(condition.evaluate("XABCY"));
        assertFalse(condition.evaluate("xyz"));
        
        // Test REGEX operator
        condition.setOperator("REGEX");
        condition.setValue("^\\d{3}-\\d{2}-\\d{4}$");
        assertTrue(condition.evaluate("123-45-6789"));
        assertFalse(condition.evaluate("123456789"));
        
        // Test numeric operators
        condition.setOperator(">");
        condition.setValue(25);
        assertTrue(condition.evaluate("30"));
        assertFalse(condition.evaluate("20"));
        assertFalse(condition.evaluate("25"));
        
        condition.setOperator("<=");
        condition.setValue(25);
        assertTrue(condition.evaluate("25"));
        assertTrue(condition.evaluate("20"));
        assertFalse(condition.evaluate("30"));
        
        // Test BETWEEN operator
        condition.setOperator("BETWEEN");
        condition.setMin(20);
        condition.setMax(30);
        assertTrue(condition.evaluate("25"));
        assertTrue(condition.evaluate("20"));
        assertTrue(condition.evaluate("30"));
        assertFalse(condition.evaluate("19"));
        assertFalse(condition.evaluate("31"));
    }

    @Test
    void testComplexScenarios() {
        // Test rule short-circuiting with MASK rules
        String maskedName = ruleEngine.applyRules("name", "Michael Chen-Smith");
        assertEquals("*********", maskedName, "MASK rule should stop further processing");
        
        // Test multiple conditions on same field
        String youngAge = ruleEngine.applyRules("age", "18");
        assertEquals("Young", youngAge);
        
        String adultAge = ruleEngine.applyRules("age", "30");
        assertEquals("Adult", adultAge);
        
        String seniorAge = ruleEngine.applyRules("age", "50");
        assertEquals("Senior", seniorAge);
    }

    @Test
    void testErrorHandling() {
        // Test invalid regex in REPLACE rule
        String result = ruleEngine.applyRules("comments", "test text");
        assertNotNull(result, "Should handle invalid patterns gracefully");
        
        // Test invalid number format for age categorization
        String nonNumericAge = ruleEngine.applyRules("age", "abc");
        assertEquals("abc", nonNumericAge, "Should return original value when can't parse number");
        
        // Test invalid date format
        String invalidDate = ruleEngine.applyRules("birthDate", "not-a-date");
        assertEquals("not-a-date", invalidDate, "Should return original value for invalid date");
    }
}