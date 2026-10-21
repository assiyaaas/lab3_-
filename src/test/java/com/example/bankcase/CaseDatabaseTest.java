package com.example.bankcase;

import com.example.bankcase.config.BankCaseManagementApplication;
import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.persistence.CaseJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs against the real PostgreSQL database (css / css on localhost).
 * No @Transactional on this class or its methods: the counts must see what was really committed.
 */
@SpringBootTest(classes = BankCaseManagementApplication.class)
class CaseDatabaseTest {

    @Autowired
    private CaseService service;

    @Autowired
    private CaseJpa cases;

    @Autowired
    private JdbcTemplate jdbc;

    private final CaseNumber number = new CaseNumber("CASE-001");

    /** Flyway created the table (V1__create_bank_case.sql). Delete rows, never drop the table. */
    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM bank_case");
    }

    /** One transaction, second INSERT fails -> everything rolled back -> count 0. */
    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateCase.class, () -> service.insertTwice(number));

        assertEquals(0, cases.count(number));
    }

    /** Two separate transactions: first commits, second fails -> first row stays -> count 1. */
    @Test
    void secondRequestKeepsTheFirst() {
        service.register(CaseId.newId(), number, "Card blocked");

        assertThrows(DuplicateCase.class,
                () -> service.register(CaseId.newId(), number, "Card blocked again"));

        assertEquals(1, cases.count(number));
    }
}