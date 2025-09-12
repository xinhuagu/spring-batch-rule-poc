package com.accenture.poc1.rule.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleConfiguration {
    private List<Rule> rules;
    private Map<String, Object> metadata;
}