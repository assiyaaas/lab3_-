package com.example.bankcase.domain;

/**
 * What the service hands to a caller: id, business key, status token, title.
 * The JPA entity never leaves the service; the web layer copies these fields into its response.
 * status is the token stored in the CHECK: New, InProgress, Resolved.
 */
public record CaseView(CaseId id, CaseNumber number, String status, String title) {
}
