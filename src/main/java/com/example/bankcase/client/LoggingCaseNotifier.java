package com.example.bankcase.client;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporary adapter for the CaseNotifier port: only writes to the log.
 * A real HTTP/SMS call replaces it in a later lab.
 */
@Component
public class LoggingCaseNotifier implements CaseNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingCaseNotifier.class);

    @Override
    public void caseMoved(CaseId id, CaseStatus newStatus) {
        log.info("Case {} moved to {}", id.value(), newStatus);
    }
}