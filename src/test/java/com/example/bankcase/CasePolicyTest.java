package com.example.bankcase;

import com.example.bankcase.CasePolicy;
import com.example.bankcase.CaseStatus;
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
                assertEquals(toStatus.getClass(), result.getClass());
            });
        } else {
            assertThrows(
                    IllegalStateException.class,
                    () -> CasePolicy.move(fromStatus, toStatus)
            );
        }
    }

    private CaseStatus statusFromString(String status) {
        return switch (status.trim()) {
            case "New" -> new CaseStatus.New();
            case "InProgress" -> new CaseStatus.InProgress();
            case "Resolved" -> new CaseStatus.Resolved();
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }
}