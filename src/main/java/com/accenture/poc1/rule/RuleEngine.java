package com.accenture.poc1.rule;

import com.accenture.poc1.rule.TransformationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RuleEngine {

    private final List<TransformationRule> rules;

    public RuleEngine() {
        this.rules = loadRules();
    }

    private List<TransformationRule> loadRules() {
        List<TransformationRule> ruleList = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("rules.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue; // Skip comments and empty lines
                    }
                    
                    TransformationRule rule = parseRule(line);
                    if (rule != null) {
                        ruleList.add(rule);
                        log.debug("Loaded rule: {}", rule);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to load rules from rules.txt", e);
        }
        
        log.info("Loaded {} transformation rules", ruleList.size());
        return ruleList;
    }

    private TransformationRule parseRule(String line) {
        String[] parts = line.split(":");
        if (parts.length != 4) {
            log.warn("Invalid rule format: {}", line);
            return null;
        }

        String fieldName = parts[0].trim();
        String ruleType = parts[1].trim();
        String condition = parts[2].trim();
        String action = parts[3].trim();

        return new TransformationRule(fieldName, ruleType, condition, action);
    }

    public String applyRules(String fieldName, Object value) {
        if (value == null) {
            return null;
        }

        String result = value.toString();
        
        for (TransformationRule rule : rules) {
            if (rule.getFieldName().equalsIgnoreCase(fieldName)) {
                result = applyRule(rule, result);
            }
        }
        
        return result;
    }

    private String applyRule(TransformationRule rule, String value) {
        switch (rule.getRuleType().toUpperCase()) {
            case "CATEGORIZE":
                return applyCategorizeRule(rule, value);
            case "TRANSFORM":
                return applyTransformRule(rule, value);
            default:
                log.warn("Unknown rule type: {}", rule.getRuleType());
                return value;
        }
    }

    private String applyCategorizeRule(TransformationRule rule, String value) {
        try {
            int intValue = Integer.parseInt(value);
            String condition = rule.getCondition();
            
            if (condition.startsWith("<=")) {
                int threshold = Integer.parseInt(condition.substring(2));
                if (intValue <= threshold) {
                    return rule.getAction();
                }
            } else if (condition.startsWith(">")) {
                int threshold = Integer.parseInt(condition.substring(1));
                if (intValue > threshold) {
                    return rule.getAction();
                }
            } else if (condition.contains("-")) {
                String[] range = condition.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                if (intValue >= min && intValue <= max) {
                    return rule.getAction();
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Cannot parse numeric value for categorization: {}", value);
        }
        
        return value; // Return original if no rule matches
    }

    private String applyTransformRule(TransformationRule rule, String value) {
        String condition = rule.getCondition();
        String action = rule.getAction();
        
        if ("*".equals(condition)) { // Apply to all values
            switch (action.toUpperCase()) {
                case "UPPERCASE":
                    return value.toUpperCase();
                case "LOWERCASE":
                    return value.toLowerCase();
                case "TITLECASE":
                    return toTitleCase(value);
                default:
                    log.warn("Unknown transform action: {}", action);
                    return value;
            }
        }
        
        return value;
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
}