package com.example.bankcase.domain;

import java.util.List;

public class RuleChain implements Rule {

    private final List<Rule> rules;

    public RuleChain(List<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("RuleChain needs at least one rule");
        }
        this.rules = List.copyOf(rules);
    }

    @Override
    public void check(CaseStatus from, CaseStatus to) {
        for (Rule rule : rules) {
            rule.check(from, to);
        }
    }
}