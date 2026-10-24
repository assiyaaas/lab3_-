package com.example.bankcase.domain;

/**
 * Outbound port: tells the outside world (the customer, another bank system)
 * that a case moved to a new status.
 * Domain only knows this interface; how the message is sent lives in client.
 */
public interface CaseNotifier {

    void caseMoved(CaseId id, CaseStatus newStatus);
}