package com.example.bankcase.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.bankcase")
public class BankCaseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCaseManagementApplication.class, args);
    }
}