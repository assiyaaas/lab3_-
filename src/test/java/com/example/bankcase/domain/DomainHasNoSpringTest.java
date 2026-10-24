package com.example.bankcase.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainHasNoSpringTest {

    @Test
    void domainSourcesDoNotImportSpringOrJdbc() throws IOException {
        Path domain = Path.of("src/main/java/com/example/bankcase/domain");
        try (Stream<Path> files = Files.walk(domain)) {
            List<Path> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(DomainHasNoSpringTest::mentionsForbidden)
                    .toList();
            assertTrue(offenders.isEmpty(), "Spring or JDBC found in domain: " + offenders);
        }
    }

    private static boolean mentionsForbidden(Path file) {
        try {
            String code = Files.readString(file);
            return code.contains("org.springframework") || code.contains("java.sql");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}