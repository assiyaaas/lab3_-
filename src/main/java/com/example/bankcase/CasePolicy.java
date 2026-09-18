package com.example.bankcase;

public class CasePolicy {

    public static CaseStatus move(CaseStatus from, CaseStatus to) {
        if (from instanceof CaseStatus.New && to instanceof CaseStatus.InProgress) {
            return to;
        }
        if (from instanceof CaseStatus.InProgress && to instanceof CaseStatus.Resolved) {
            return to;
        }

        throw new IllegalStateException("Transition from " + from.getClass().getSimpleName() +
                " to " + to.getClass().getSimpleName() + " is not allowed.");
    }
}