package com.example.bankcase.domain;

public class MissingCase extends RuntimeException {

    public MissingCase(CaseId id) {
        super("case not found: " + id.value());
    }
}
