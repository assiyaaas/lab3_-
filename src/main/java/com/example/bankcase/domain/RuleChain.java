package com.example.bankcase.domain;

import java.util.List;

public class RuleChain implements Rule {

    private final List<Rule> rules;

    public RuleChain(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public void check(CaseStatus from, CaseStatus to) {
        for (Rule rule : rules) {
            rule.check(from, to);
        }
    }
}