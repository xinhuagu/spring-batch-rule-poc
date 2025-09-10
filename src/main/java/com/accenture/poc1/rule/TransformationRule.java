package com.accenture.poc1.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformationRule {
    private String fieldName;
    private String ruleType;
    private String condition;
    private String action;

    @Override
    public String toString() {
        return String.format("Rule[%s:%s:%s:%s]", fieldName, ruleType, condition, action);
    }
}