package com.example.bankcase.domain;

import java.util.UUID;

public record CaseId(UUID value) {

    public CaseId {
        if (value == null) {
            throw new IllegalArgumentException("CaseId cannot be null");
        }
    }


    public static CaseId newId() {
        return new CaseId(UUID.randomUUID());
    }

    public static CaseId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("CaseId cannot be null or blank");
        }
        return new CaseId(UUID.fromString(raw.trim()));
    }
}