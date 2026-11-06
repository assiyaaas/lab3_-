package com.example.bankcase.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

/**
 * app.downstream.* from yaml: where the outbound notifier would call, and how long it may wait.
 * Bound and checked at startup; Lab 6 makes the real call and enforces the timeout.
 */
@Validated
@ConfigurationProperties(prefix = "app.downstream")
public record DownstreamProperties(@NotNull URI baseUrl, @NotNull Duration timeout) {
}
