package com.example.bankcase.domain;

public record CaseId(String value) {
    public CaseId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CaseId cannot be null or blank");
        }
    }
}