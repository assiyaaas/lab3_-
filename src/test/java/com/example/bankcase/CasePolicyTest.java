package com.example.bankcase;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CasePolicy;
import com.example.bankcase.domain.CaseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CasePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "New, InProgress, true",
            "InProgress, Resolved, true",
            "New, Resolved, false",
            "Resolved, New, false"
    })
    void testStatusTransitions(String from, String to, boolean allowed) {

        CaseStatus fromStatus = statusFromString(from);
        CaseStatus toStatus = statusFromString(to);

        if (allowed) {
            assertDoesNotThrow(() -> {
                CaseStatus result = CasePolicy.move(fromStatus, toStatus);
                assertEquals(toStatus, result);
            });
        } else {
            assertThrows(
                    IllegalStateException.class,
                    () -> CasePolicy.move(fromStatus, toStatus)
            );
        }
    }

    @Test
    void caseIdCannotBeNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CaseId(null)
        );
    }

    @Test
    void caseIdCannotBeEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CaseId("")
        );
    }

    @Test
    void caseIdCannotBeBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CaseId("   ")
        );
    }

    @Test
    void validCaseIdIsAccepted() {
        CaseId id = new CaseId("CASE-001");

        assertEquals("CASE-001", id.value());
    }

    private CaseStatus statusFromString(String status) {
        return switch (status) {
            case "New" -> new CaseStatus.New();
            case "InProgress" -> new CaseStatus.InProgress();
            case "Resolved" -> new CaseStatus.Resolved();
            default -> throw new IllegalArgumentException("Unknown status");
        };
    }
}