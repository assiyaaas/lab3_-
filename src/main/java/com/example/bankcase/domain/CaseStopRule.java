package com.example.bankcase.domain;

public class CaseStopRule implements Rule {

    @Override
    public void check(CaseStatus from, CaseStatus to) {
        if (from instanceof CaseStatus.Resolved) {
            throw new IllegalStateException(
                    "A resolved case cannot be changed."
            );
        }
    }
}