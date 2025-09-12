package com.accenture.poc1.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private String id;
    private String fieldName;
    private String type;
    private int priority;
    private boolean enabled;
    private String description;
    private RuleCondition condition;
    private Object action; // Can be String or Map<String, Object>
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        private String operator;
        private Object value;
        private Integer min;
        private Integer max;
        @Builder.Default
        private boolean caseSensitive = true;
        
        public boolean evaluate(String input) {
            if (input == null) {
                return "NULL".equals(operator) || "IS_NULL".equals(operator);
            }
            
            switch (operator.toUpperCase()) {
                case "NOT_NULL":
                    return input != null && !input.trim().isEmpty();
                case "EQUALS":
                    return caseSensitive ? 
                        input.equals(String.valueOf(value)) : 
                        input.equalsIgnoreCase(String.valueOf(value));
                case "CONTAINS":
                    return caseSensitive ? 
                        input.contains(String.valueOf(value)) : 
                        input.toUpperCase().contains(String.valueOf(value).toUpperCase());
                case "STARTS_WITH":
                    return caseSensitive ?
                        input.startsWith(String.valueOf(value)) :
                        input.toUpperCase().startsWith(String.valueOf(value).toUpperCase());
                case "ENDS_WITH":
                    return caseSensitive ?
                        input.endsWith(String.valueOf(value)) :
                        input.toUpperCase().endsWith(String.valueOf(value).toUpperCase());
                case "REGEX":
                    // Use find() to search for pattern anywhere in the string
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        String.valueOf(value), 
                        caseSensitive ? 0 : java.util.regex.Pattern.CASE_INSENSITIVE
                    );
                    return pattern.matcher(input).find();
                case "BETWEEN":
                    try {
                        int numValue = Integer.parseInt(input);
                        return numValue >= min && numValue <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case ">":
                    try {
                        return Integer.parseInt(input) > Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case ">=":
                    try {
                        return Integer.parseInt(input) >= Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "<":
                    try {
                        return Integer.parseInt(input) < Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "<=":
                    try {
                        return Integer.parseInt(input) <= Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                default:
                    return false;
            }
        }
    }
}