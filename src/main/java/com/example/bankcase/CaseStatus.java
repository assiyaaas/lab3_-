package com.example.bankcase;

public sealed interface CaseStatus permits CaseStatus.New, CaseStatus.InProgress, CaseStatus.Resolved {

    record New() implements CaseStatus {
        @Override public String toString() { return "New"; }
    }

    record InProgress() implements CaseStatus {
        @Override public String toString() { return "InProgress"; }
    }

    record Resolved() implements CaseStatus {
        @Override public String toString() { return "Resolved"; }
    }
}