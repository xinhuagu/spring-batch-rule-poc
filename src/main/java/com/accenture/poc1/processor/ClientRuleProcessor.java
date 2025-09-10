package com.accenture.poc1.processor;

import com.accenture.poc1.model.Client;
import com.accenture.poc1.rule.RuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientRuleProcessor implements ItemProcessor<Client, Client> {

    private final RuleEngine ruleEngine;

    @Override
    public Client process(Client item) throws Exception {
        if (item == null) {
            return null;
        }

        log.debug("Processing client before rules: {}", item);

        // Create a new client object with transformed data
        Client transformedClient = new Client();
        transformedClient.setId(item.getId());

        // Apply rules to name field
        String transformedName = ruleEngine.applyRules("name", item.getName());
        transformedClient.setName(transformedName);

        // Apply rules to age field (this will convert age to category)
        String ageCategory = ruleEngine.applyRules("age", item.getAge());
        
        // Set both original age and age category
        transformedClient.setAge(item.getAge());
        transformedClient.setAgeCategory(ageCategory);
        
        log.debug("Processing client after rules: {} -> Name: {}, Age category: {}", 
                 item, transformedName, ageCategory);

        return transformedClient;
    }
}