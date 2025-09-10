package com.accenture.poc1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private Integer id;
    private String name;
    private Integer age;
    private String ageCategory; // For age categorization rule
    
    public Client(Integer id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}