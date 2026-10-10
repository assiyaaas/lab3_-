package com.example.bankcase;

import com.example.bankcase.config.BankCaseManagementApplication;
import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.persistence.CaseJdbc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private CaseJdbc cases;

    @Autowired
    private JdbcTemplate jdbc;

    private final CaseNumber number = new CaseNumber("CASE-001");

    /** Every test starts from an empty table built from schema.sql. */
    @BeforeEach
    void cleanDatabase() throws IOException {
        jdbc.execute("DROP TABLE IF EXISTS bank_case");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
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