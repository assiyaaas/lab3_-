package com.example.bankcase;

import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.Rule;
import org.springframework.stereotype.Service;

@Service
public class CaseService {

    private final Rule rules;

    public CaseService(Rule rules) {
        this.rules = rules;
    }

    public CaseStatus move(CaseStatus from, CaseStatus to) {
        rules.check(from, to);
        return to;
    }
}