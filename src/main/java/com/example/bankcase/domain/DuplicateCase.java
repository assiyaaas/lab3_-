package com.example.bankcase.domain;

public class DuplicateCase extends RuntimeException {

    public DuplicateCase(CaseNumber number) {
        super("duplicate case: " + number.value());
    }
}