package com.example.bankcase.domain;

public class TransitionRule implements Rule {

    @Override
    public void check(CaseStatus from, CaseStatus to) {
        CasePolicy.move(from, to);
    }
}