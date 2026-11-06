package com.example.bankcase.handler;

import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.domain.MissingCase;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

/**
 * The only place that knows 400, 404, 409, 415 and 500.
 * Domain exceptions get a status here; the stack trace stays in the log.
 */
@RestControllerAdvice
public class ApiErrors {

    static final String CORRELATION_HEADER = "X-Correlation-Id";

    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error == null
                ? "request is not valid"
                : error.getField() + " " + error.getDefaultMessage();
        return body(HttpStatus.BAD_REQUEST, "VALIDATION", message, req);
    }

    // GET /api/v1/bank-cases/CASE-001: the business key is not the path id
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> badPathId(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "VALIDATION", ex.getName() + " must be a UUID", req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> notJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", req);
    }

    @ExceptionHandler(MissingCase.class)
    ResponseEntity<ApiError> missing(MissingCase ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateCase.class)
    ResponseEntity<ApiError> duplicate(DuplicateCase ex, HttpServletRequest req) {
        return body(HttpStatus.CONFLICT, "DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> unsupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED", "content type must be application/json", req);
    }

    // an unknown path (or an actuator endpoint that is not exposed) stays 404, not 500
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> noSuchPath(NoResourceFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", "no such path", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> wrongVerb(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMethod() + " is not supported here", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        String correlationId = correlation(req);
        log.error("unexpected error, correlationId={}", correlationId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL", "unexpected error", correlationId));
    }

    private static ResponseEntity<ApiError> body(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(code, message, correlation(req)));
    }

    // echo the caller's id; mint one when the header is missing or blank (week 13 moves this into a filter)
    static String correlation(HttpServletRequest req) {
        String id = req.getHeader(CORRELATION_HEADER);
        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }
}
