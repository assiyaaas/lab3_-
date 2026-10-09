package com.example.bankcase;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CasePolicy;
import com.example.bankcase.domain.CaseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    // ---------- CaseId (UUID, surrogate key) ----------

    @Test
    void caseIdCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new CaseId(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void caseIdFromBlankTextThrows(String raw) {
        assertThrows(IllegalArgumentException.class, () -> CaseId.of(raw));
    }

    @Test
    void newIdIsUnique() {
        assertNotEquals(CaseId.newId(), CaseId.newId());
    }

    @Test
    void caseIdReadsBackFromText() {
        CaseId id = CaseId.newId();
        assertEquals(id, CaseId.of(id.value().toString()));
    }

    // ---------- CaseNumber (CASE-001, business key) ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void caseNumberCannotBeNullOrBlank(String raw) {
        assertThrows(IllegalArgumentException.class, () -> new CaseNumber(raw));
    }

    @Test
    void validCaseNumberIsAccepted() {
        CaseNumber number = new CaseNumber("CASE-001");
        assertEquals("CASE-001", number.value());
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