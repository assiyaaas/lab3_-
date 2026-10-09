package com.example.bankcase.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CaseJdbc {

    private final JdbcTemplate jdbc;

    public CaseJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String businessKey, String status, String title) {
        jdbc.update("""
                INSERT INTO bank_case (id, business_key, status, title)
                VALUES (?, ?, ?, ?)
                """, id, businessKey, status, title);
    }

    public int count(String businessKey) {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM bank_case WHERE business_key = ?",
                Integer.class,
                businessKey
        );
        return count == null ? 0 : count;
    }
}
