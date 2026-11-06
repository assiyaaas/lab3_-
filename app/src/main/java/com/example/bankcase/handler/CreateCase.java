package com.example.bankcase.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * POST body. Validation lives here, in the web package, never in domain.
 * Messages are written out so the text does not depend on the JVM locale.
 */
public record CreateCase(
        @NotBlank(message = "must not be blank") String businessKey,
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "size must be between 0 and 120") String title) {
}
