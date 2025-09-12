package com.accenture.poc1.rule;

import com.accenture.poc1.rule.model.Rule;
import com.accenture.poc1.rule.model.RuleConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EnhancedRuleEngine {
    
    private final List<Rule> rules;
    private final Map<String, List<Rule>> rulesByField;
    private final Map<String, Long> ruleExecutionCount;
    private final Map<String, Long> ruleExecutionTime;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public EnhancedRuleEngine() {
        this.rules = loadRules();
        this.rulesByField = indexRulesByField();
        this.ruleExecutionCount = new ConcurrentHashMap<>();
        this.ruleExecutionTime = new ConcurrentHashMap<>();
    }
    
    private List<Rule> loadRules() {
        List<Rule> loadedRules = new ArrayList<>();
        
        // Try loading JSON rules first
        try {
            ClassPathResource jsonResource = new ClassPathResource("rules.json");
            if (jsonResource.exists()) {
                try (InputStream is = jsonResource.getInputStream()) {
                    RuleConfiguration config = objectMapper.readValue(is, RuleConfiguration.class);
                    loadedRules.addAll(config.getRules());
                    log.info("Loaded {} rules from rules.json", config.getRules().size());
                }
            }
        } catch (IOException e) {
            log.error("Failed to load rules from rules.json", e);
        }
        
        // Sort rules by priority (lower number = higher priority)
        loadedRules.sort(Comparator.comparingInt(Rule::getPriority));
        
        return loadedRules;
    }
    
    private Map<String, List<Rule>> indexRulesByField() {
        return rules.stream()
            .filter(Rule::isEnabled)
            .collect(Collectors.groupingBy(
                rule -> rule.getFieldName().toLowerCase(),
                Collectors.toList()
            ));
    }
    
    public String applyRules(String fieldName, Object value) {
        if (value == null) {
            return null;
        }
        
        String result = value.toString();
        List<Rule> fieldRules = rulesByField.get(fieldName.toLowerCase());
        
        if (fieldRules == null || fieldRules.isEmpty()) {
            return result;
        }
        
        for (Rule rule : fieldRules) {
            long startTime = System.currentTimeMillis();
            
            try {
                // For VALIDATE rules, we need different logic
                if ("VALIDATE".equals(rule.getType())) {
                    String newResult = applyValidateRule(rule, result);
                    trackRuleExecution(rule, startTime);
                    
                    // If validation changed the value (null or default), stop processing
                    if (newResult == null || !newResult.equals(result)) {
                        return newResult;
                    }
                } else if ("REPLACE".equals(rule.getType())) {
                    // REPLACE rules need special handling
                    String newResult = applyReplaceRule(rule, result);
                    trackRuleExecution(rule, startTime);
                    result = newResult;
                } else if (rule.getCondition().evaluate(result)) {
                    String newResult = applyRule(rule, result);
                    
                    // Track metrics
                    trackRuleExecution(rule, startTime);
                    
                    // If MASK rule changes value, stop processing
                    if (!newResult.equals(result) && "MASK".equals(rule.getType())) {
                        return newResult;
                    }
                    
                    result = newResult;
                }
            } catch (Exception e) {
                log.error("Error applying rule {} to field {}: {}", 
                    rule.getId(), fieldName, e.getMessage());
            }
        }
        
        return result;
    }
    
    private String applyRule(Rule rule, String value) {
        switch (rule.getType().toUpperCase()) {
            case "CATEGORIZE":
                return applyCategorizeRule(rule, value);
            case "TRANSFORM":
                return applyTransformRule(rule, value);
            case "MASK":
                return applyMaskRule(rule, value);
            case "VALIDATE":
                return applyValidateRule(rule, value);
            case "FORMAT":
                return applyFormatRule(rule, value);
            case "REPLACE":
                return applyReplaceRule(rule, value);
            default:
                log.warn("Unknown rule type: {}", rule.getType());
                return value;
        }
    }
    
    private String applyCategorizeRule(Rule rule, String value) {
        // For CATEGORIZE rules, the action is a simple string
        return rule.getAction().toString();
    }
    
    private String applyTransformRule(Rule rule, String value) {
        String action = rule.getAction().toString();
        
        switch (action.toUpperCase()) {
            case "UPPERCASE":
                return value.toUpperCase();
            case "LOWERCASE":
                return value.toLowerCase();
            case "TITLECASE":
                return toTitleCase(value);
            case "TRIM":
                return value.trim();
            case "REVERSE":
                return new StringBuilder(value).reverse().toString();
            default:
                log.warn("Unknown transform action: {}", action);
                return value;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String applyMaskRule(Rule rule, String value) {
        Map<String, Object> action = (Map<String, Object>) rule.getAction();
        String maskType = (String) action.get("maskType");
        String maskChar = (String) action.getOrDefault("maskChar", "*");
        
        switch (maskType.toUpperCase()) {
            case "FULL":
                int length = (Integer) action.getOrDefault("length", value.length());
                return maskChar.repeat(length);
                
            case "PARTIAL":
                int showFirst = (Integer) action.getOrDefault("showFirst", 0);
                int showLast = (Integer) action.getOrDefault("showLast", 0);
                boolean preserveFormat = (Boolean) action.getOrDefault("preserveFormat", false);
                
                if (preserveFormat) {
                    return applyFormatPreservingMask(value, maskChar, showFirst, showLast);
                } else {
                    return applyPartialMask(value, maskChar, showFirst, showLast);
                }
                
            case "RANDOM":
                return generateRandomMask(value.length());
                
            default:
                log.warn("Unknown mask type: {}", maskType);
                return value;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String applyValidateRule(Rule rule, String value) {
        Map<String, Object> action = (Map<String, Object>) rule.getAction();
        String onInvalid = (String) action.get("onInvalid");
        
        // Check if the value is valid according to the condition
        boolean isValid = rule.getCondition().evaluate(value);
        
        if (!isValid) {
            // Handle invalid case
            switch (onInvalid.toUpperCase()) {
                case "REJECT":
                    String errorMessage = (String) action.getOrDefault("errorMessage", "Validation failed");
                    log.warn("Validation failed for value: {} - {}", value, errorMessage);
                    return null; // Or throw exception
                    
                case "DEFAULT":
                    return (String) action.getOrDefault("defaultValue", "");
                    
                case "SKIP":
                    return value;
                    
                default:
                    return value;
            }
        }
        
        // Value is valid, return as-is
        return value;
    }
    
    @SuppressWarnings("unchecked")
    private String applyFormatRule(Rule rule, String value) {
        Map<String, Object> action = (Map<String, Object>) rule.getAction();
        String formatType = (String) action.get("formatType");
        
        switch (formatType.toUpperCase()) {
            case "PHONE":
                return formatPhone(value, (String) action.get("pattern"));
                
            case "DATE":
                return formatDate(value, 
                    (String) action.get("inputFormat"), 
                    (String) action.get("outputFormat"));
                    
            case "NUMBER":
                return formatNumber(value, (String) action.get("pattern"));
                
            case "CURRENCY":
                return formatCurrency(value, (String) action.getOrDefault("locale", "en-US"));
                
            default:
                log.warn("Unknown format type: {}", formatType);
                return value;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String applyReplaceRule(Rule rule, String value) {
        Map<String, Object> action = (Map<String, Object>) rule.getAction();
        String replaceWith = (String) action.get("replaceWith");
        
        // Only replace if condition matches
        if (rule.getCondition().evaluate(value)) {
            // Use the regex from condition to find and replace
            String regex = String.valueOf(rule.getCondition().getValue());
            boolean caseSensitive = rule.getCondition().isCaseSensitive();
            
            Pattern pattern = caseSensitive ? 
                Pattern.compile(regex) : 
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                
            Matcher matcher = pattern.matcher(value);
            return matcher.replaceAll(replaceWith);
        }
        
        return value;
    }
    
    private String applyFormatPreservingMask(String value, String maskChar, int showFirst, int showLast) {
        StringBuilder result = new StringBuilder();
        int length = value.length();
        
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                // Preserve special characters
                result.append(c);
            } else if (i < showFirst || i >= length - showLast) {
                // Show first/last characters
                result.append(c);
            } else {
                // Mask middle characters
                result.append(maskChar);
            }
        }
        
        return result.toString();
    }
    
    private String applyPartialMask(String value, String maskChar, int showFirst, int showLast) {
        if (value.length() <= showFirst + showLast) {
            return value; // Too short to mask
        }
        
        String first = value.substring(0, showFirst);
        String last = value.substring(value.length() - showLast);
        int middleLength = value.length() - showFirst - showLast;
        
        return first + maskChar.repeat(middleLength) + last;
    }
    
    private String generateRandomMask(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return result.toString();
    }
    
    private String formatPhone(String value, String pattern) {
        // Remove non-digits
        String digits = value.replaceAll("[^0-9]", "");
        
        if (digits.length() != 10) {
            return value; // Can't format non-10-digit numbers
        }
        
        // Apply pattern (e.g., "({0}{1}{2}) {3}{4}{5}-{6}{7}{8}{9}")
        String result = pattern;
        for (int i = 0; i < digits.length(); i++) {
            result = result.replace("{" + i + "}", String.valueOf(digits.charAt(i)));
        }
        
        return result;
    }
    
    private String formatDate(String value, String inputFormat, String outputFormat) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(inputFormat);
            SimpleDateFormat output = new SimpleDateFormat(outputFormat);
            Date date = input.parse(value);
            return output.format(date);
        } catch (Exception e) {
            log.warn("Failed to format date: {}", value);
            return value;
        }
    }
    
    private String formatNumber(String value, String pattern) {
        try {
            double number = Double.parseDouble(value);
            java.text.DecimalFormat df = new java.text.DecimalFormat(pattern);
            return df.format(number);
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    private String formatCurrency(String value, String locale) {
        try {
            double amount = Double.parseDouble(value);
            Locale loc = Locale.forLanguageTag(locale);
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(loc);
            return formatter.format(amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    private String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c);
            }
            titleCase.append(c);
        }
        
        return titleCase.toString();
    }
    
    private void trackRuleExecution(Rule rule, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        ruleExecutionCount.merge(rule.getId(), 1L, Long::sum);
        ruleExecutionTime.merge(rule.getId(), executionTime, Long::sum);
    }
    
    // Metrics and monitoring methods
    public Map<String, RuleMetrics> getRuleMetrics() {
        Map<String, RuleMetrics> metrics = new HashMap<>();
        
        for (Rule rule : rules) {
            String id = rule.getId();
            long count = ruleExecutionCount.getOrDefault(id, 0L);
            long totalTime = ruleExecutionTime.getOrDefault(id, 0L);
            double avgTime = count > 0 ? (double) totalTime / count : 0;
            
            RuleMetrics ruleMetrics = new RuleMetrics();
            ruleMetrics.setRuleId(id);
            ruleMetrics.setExecutionCount(count);
            ruleMetrics.setTotalExecutionTime(totalTime);
            ruleMetrics.setAverageExecutionTime(avgTime);
            metrics.put(id, ruleMetrics);
        }
        
        return metrics;
    }
    
    @Data
    public static class RuleMetrics {
        private String ruleId;
        private long executionCount;
        private long totalExecutionTime;
        private double averageExecutionTime;
    }
    
    // Method to reload rules without restart
    public void reloadRules() {
        log.info("Reloading rules...");
        List<Rule> newRules = loadRules();
        
        synchronized (this) {
            this.rules.clear();
            this.rules.addAll(newRules);
            this.rulesByField.clear();
            this.rulesByField.putAll(indexRulesByField());
        }
        
        log.info("Rules reloaded successfully. Total rules: {}", rules.size());
    }
    
    // Get all active rules for a field
    public List<Rule> getRulesForField(String fieldName) {
        return rulesByField.getOrDefault(fieldName.toLowerCase(), Collections.emptyList());
    }
}