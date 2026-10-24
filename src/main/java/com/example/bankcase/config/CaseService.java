package com.example.bankcase.config;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.Rule;
import org.springframework.stereotype.Service;

@Service
public class CaseService {

    private final Rule rules;
    private final CaseNotifier notifier;

    public CaseService(Rule rules, CaseNotifier notifier) {
        this.rules = rules;
        this.notifier = notifier;
    }

    public CaseStatus move(CaseId id, CaseStatus from, CaseStatus to) {
        rules.check(from, to);          // forbidden move throws here, notifier is never called
        notifier.caseMoved(id, to);     // only after the move is allowed
        return to;
    }
}