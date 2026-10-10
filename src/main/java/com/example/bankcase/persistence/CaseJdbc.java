package com.example.bankcase.persistence;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CaseJdbc {

    private final JdbcTemplate jdbc;

    public CaseJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(CaseId id, CaseNumber number, CaseStatus status, String title) {
        jdbc.update("""
                INSERT INTO bank_case (id, business_key, status, title)
                VALUES (?, ?, ?, ?)
                """, id.value(), number.value(), toText(status), title);
    }

    public int count(CaseNumber number) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM bank_case WHERE business_key = ?",
                Integer.class,
                number.value());
        return n == null ? 0 : n;
    }

    // sealed status -> text column
    static String toText(CaseStatus status) {
        return switch (status) {
            case CaseStatus.New n -> "New";
            case CaseStatus.InProgress p -> "InProgress";
            case CaseStatus.Resolved r -> "Resolved";
        };
    }

    // text column -> sealed status; unknown text throws, never becomes a new status
    static CaseStatus fromText(String text) {
        return switch (text) {
            case "New" -> new CaseStatus.New();
            case "InProgress" -> new CaseStatus.InProgress();
            case "Resolved" -> new CaseStatus.Resolved();
            default -> throw new IllegalStateException("Unknown status in database: " + text);
        };
    }
}