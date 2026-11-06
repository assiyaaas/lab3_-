package com.example.bankcase.client;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Outbound port adapter. The downstream URL and timeout come from yaml; no network call yet (Lab 6). */
@Component
public class LoggingCaseNotifier implements CaseNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingCaseNotifier.class);

    private final DownstreamProperties downstream;

    public LoggingCaseNotifier(DownstreamProperties downstream) {
        this.downstream = downstream;
    }

    @Override
    public void caseMoved(CaseId id, CaseStatus newStatus) {
        log.info("Case {} moved to {} (downstream {}, timeout {})",
                id.value(), newStatus, downstream.baseUrl(), downstream.timeout());
    }
}
