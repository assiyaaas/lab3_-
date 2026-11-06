package com.example.bankcase;

import com.example.bankcase.client.DownstreamProperties;
import com.example.bankcase.config.BankCaseManagementApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** The dev profile binds app.downstream from yaml. Nothing opens a socket to port 9090. */
@SpringBootTest(classes = BankCaseManagementApplication.class)
@ActiveProfiles("dev")
class DownstreamPropertiesTest {

    @Autowired
    private DownstreamProperties downstream;

    @Test
    void devProfileReadsDownstreamFromYaml() {
        assertAll("app.downstream on dev",
                () -> assertEquals(URI.create("http://localhost:9090"), downstream.baseUrl()),
                () -> assertEquals(Duration.ofSeconds(2), downstream.timeout()));
    }
}
