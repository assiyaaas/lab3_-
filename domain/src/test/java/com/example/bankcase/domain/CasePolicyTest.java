package com.example.bankcase.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CasePolicyTest {

    // ---------- Status table from the README ----------

    @Nested
    @DisplayName("Status transitions")
    class Transitions {

        @ParameterizedTest
        @CsvSource({
                "New, InProgress, true",
                "InProgress, Resolved, true",
                "New, Resolved, false",
                "Resolved, New, false"
        })
        void statusTableHolds(String from, String to, boolean allowed) {

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
        void forbiddenMoveExplainsItself() {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> CasePolicy.move(new CaseStatus.New(), new CaseStatus.Resolved()));

            // all three checks run and are reported together, even if the first one fails
            assertAll("error message",
                    () -> assertTrue(e.getMessage().contains("New")),
                    () -> assertTrue(e.getMessage().contains("Resolved")),
                    () -> assertTrue(e.getMessage().contains("not allowed")));
        }
    }

    // ---------- CaseId (UUID, surrogate key) ----------

    @Nested
    @DisplayName("CaseId")
    class CaseIdTests {

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
    }

    // ---------- CaseNumber (CASE-001, business key) ----------

    @Nested
    @DisplayName("CaseNumber")
    class CaseNumberTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void caseNumberCannotBeNullOrBlank(String raw) {
            assertThrows(IllegalArgumentException.class, () -> new CaseNumber(raw));
        }

        @Test
        void validCaseNumberIsAccepted() {
            CaseNumber number = new CaseNumber("CASE-001");

            assertAll("CaseNumber CASE-001",
                    () -> assertEquals("CASE-001", number.value()),
                    () -> assertEquals(new CaseNumber("CASE-001"), number),
                    () -> assertNotEquals(new CaseNumber("CASE-002"), number));
        }
    }

    private static CaseStatus statusFromString(String status) {
        return switch (status) {
            case "New" -> new CaseStatus.New();
            case "InProgress" -> new CaseStatus.InProgress();
            case "Resolved" -> new CaseStatus.Resolved();
            default -> throw new IllegalArgumentException("Unknown status");
        };
    }
}
