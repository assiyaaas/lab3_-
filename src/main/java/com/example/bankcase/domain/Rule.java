package com.example.bankcase.domain;

public interface Rule {

    void check(CaseStatus from, CaseStatus to);
}