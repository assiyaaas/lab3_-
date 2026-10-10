package com.example.bankcase;

import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.CaseStopRule;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.domain.RuleChain;
import com.example.bankcase.domain.TransitionRule;
import com.example.bankcase.persistence.CaseJdbc;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/** The outbound port CaseNotifier is mocked: no real SMS or HTTP call is made. */
class CaseServiceNotifierTest {

    private final CaseNotifier notifier = mock(CaseNotifier.class);
    private final CaseJdbc cases = mock(CaseJdbc.class);   // move() does not touch the database
    private final Rule rules = new RuleChain(List.of(new CaseStopRule(), new TransitionRule()));
    private final CaseService service = new CaseService(rules, notifier, cases);
    private final CaseId id = CaseId.newId();

    @Test
    void allowedMoveNotifiesOnce() {
        service.move(id, new CaseStatus.New(), new CaseStatus.InProgress());

        verify(notifier).caseMoved(id, new CaseStatus.InProgress());
    }

    @Test
    void forbiddenMoveDoesNotNotify() {
        assertThrows(IllegalStateException.class,
                () -> service.move(id, new CaseStatus.New(), new CaseStatus.Resolved()));

        verifyNoInteractions(notifier);
    }
}