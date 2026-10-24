package com.example.bankcase;

import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.CaseStopRule;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.domain.RuleChain;
import com.example.bankcase.domain.TransitionRule;
import com.example.bankcase.persistence.CaseJpa;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/** The outbound port CaseNotifier is mocked: no real SMS or HTTP call is made. */
class CaseServiceNotifierTest {

    private final CaseNotifier notifier = mock(CaseNotifier.class);
    private final CaseJpa cases = mock(CaseJpa.class);   // move() does not touch the database
    private final Rule rules = new RuleChain(List.of(new CaseStopRule(), new TransitionRule()));
    private final CaseService service = new CaseService(rules, notifier, cases);
    private final CaseId id = CaseId.newId();

    @Test
    void allowedMoveNotifiesOnceWithTheSameId() {
        service.move(id, new CaseStatus.New(), new CaseStatus.InProgress());

        // the captor answers the audit question: was the downstream called with the id from the request?
        ArgumentCaptor<CaseId> captor = ArgumentCaptor.forClass(CaseId.class);
        verify(notifier, times(1)).caseMoved(captor.capture(), eq(new CaseStatus.InProgress()));
        assertEquals(id, captor.getValue());
        verifyNoMoreInteractions(notifier);
    }

    @Test
    void forbiddenMoveDoesNotNotify() {
        assertThrows(IllegalStateException.class,
                () -> service.move(id, new CaseStatus.New(), new CaseStatus.Resolved()));

        verifyNoInteractions(notifier);
    }
}
