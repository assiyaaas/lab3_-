package com.example.bankcase.domain;

/**
 * The human case number (CASE-001) the customer sees in letters.
 * Stored in the database as business_key (UNIQUE, NOT NULL).
 */
public record CaseNumber(String value) {

    public CaseNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CaseNumber cannot be null or blank");
        }
    }
}