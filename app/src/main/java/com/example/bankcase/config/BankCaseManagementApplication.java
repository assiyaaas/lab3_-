package com.example.bankcase.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.example.bankcase")
@EntityScan("com.example.bankcase.persistence")
@ConfigurationPropertiesScan("com.example.bankcase")
public class BankCaseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCaseManagementApplication.class, args);
    }
}
