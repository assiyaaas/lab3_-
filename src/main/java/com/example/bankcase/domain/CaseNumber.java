package com.example.bankcase.domain;

public record CaseNumber(String value) {

    public CaseNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CaseNumber cannot be null or blank");
        }
    }
}