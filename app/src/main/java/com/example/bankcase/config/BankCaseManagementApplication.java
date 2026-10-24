package com.example.bankcase.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.example.bankcase")
@EntityScan("com.example.bankcase.persistence")
public class BankCaseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCaseManagementApplication.class, args);
    }
}
