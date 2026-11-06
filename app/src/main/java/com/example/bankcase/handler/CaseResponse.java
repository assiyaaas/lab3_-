package com.example.bankcase.handler;

import com.example.bankcase.domain.CaseView;

import java.util.UUID;

/** What the caller gets back. Not the JPA entity: no persistence field reaches the wire. */
public record CaseResponse(UUID id, String businessKey, String status, String title) {

    static CaseResponse from(CaseView view) {
        return new CaseResponse(view.id().value(), view.number().value(), view.status(), view.title());
    }
}
