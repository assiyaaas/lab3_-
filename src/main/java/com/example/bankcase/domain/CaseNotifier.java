package com.example.bankcase.domain;

public interface CaseNotifier {

    void caseMoved(CaseId id, CaseStatus newStatus);
}