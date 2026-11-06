package com.example.bankcase.handler;

/** Every failure body: these three fields, nothing else. No stack trace, no exception class name. */
public record ApiError(String code, String message, String correlationId) {
}
