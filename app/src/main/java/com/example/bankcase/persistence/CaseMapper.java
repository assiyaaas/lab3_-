package com.example.bankcase.persistence;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;

/** Domain types in, entity out. Only translates: the policy decides whether a move is legal. */
public final class CaseMapper {

    private CaseMapper() {
    }

    public static CaseEntity toEntity(CaseId id, CaseNumber number, CaseStatus status, String title) {
        var row = new CaseEntity();
        row.setId(id.value());
        row.setBusinessKey(number.value());
        row.setStatus(toText(status));
        row.setTitle(title);
        return row;
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
