package com.example.bankcase.domain;

/**
 * Thrown when a case with the same business number already exists.
 */
public class DuplicateCase extends RuntimeException {

    public DuplicateCase(String businessKey) {
        super("Case already exists: " + businessKey);
    }
}
