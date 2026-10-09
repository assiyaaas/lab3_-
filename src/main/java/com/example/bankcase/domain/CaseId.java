package com.example.bankcase.domain;

import java.util.UUID;

/**
 * Surrogate identity of a case: a UUID, never shown to the customer.
 * Stored in the database as the primary key (column id).
 */
public record CaseId(UUID value) {

    public CaseId {
        if (value == null) {
            throw new IllegalArgumentException("CaseId cannot be null");
        }
    }

    /** A brand new, random id for a new case. */
    public static CaseId newId() {
        return new CaseId(UUID.randomUUID());
    }

    /** Read an id back from text (for example, from the database). */
    public static CaseId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("CaseId cannot be null or blank");
        }
        return new CaseId(UUID.fromString(raw.trim()));
    }
}