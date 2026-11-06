package com.example.bankcase.config;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.CaseView;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.domain.MissingCase;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.persistence.CaseJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.bankcase.persistence.CaseMapper.toEntity;
import static com.example.bankcase.persistence.CaseMapper.toView;

@Service
public class CaseService {

    private final Rule rules;
    private final CaseNotifier notifier;
    private final CaseJpa cases;

    public CaseService(Rule rules, CaseNotifier notifier, CaseJpa cases) {
        this.rules = rules;
        this.notifier = notifier;
        this.cases = cases;
    }

    public CaseStatus move(CaseId id, CaseStatus from, CaseStatus to) {
        rules.check(from, to);
        notifier.caseMoved(id, to);
        return to;
    }

    // READ_COMMITTED is the PostgreSQL default, written out so the choice is visible:
    // no dirty reads; a duplicate business_key is stopped by UNIQUE, not by the isolation level
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CaseView register(CaseId id, CaseNumber number, String title) {
        var row = toEntity(id, number, new CaseStatus.New(), title);
        try {
            cases.insert(row);
            cases.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCase(number);
        }
        return toView(row);
    }

    // GET: loads only. No register, no move, no flush
    @Transactional(readOnly = true)
    public CaseView require(CaseId id) {
        return cases.find(id.value())
                .map(row -> toView(row))
                .orElseThrow(() -> new MissingCase(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void insertTwice(CaseNumber number) {
        cases.insert(toEntity(CaseId.newId(), number, new CaseStatus.New(), "Card blocked"));
        try {
            // a second UUID, the same business key
            cases.insert(toEntity(CaseId.newId(), number, new CaseStatus.New(), "Card blocked again"));
            cases.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCase(number);
        }
    }
}
