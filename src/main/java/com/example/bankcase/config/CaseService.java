package com.example.bankcase.config;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.persistence.CaseJdbc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseService {

    private final Rule rules;
    private final CaseNotifier notifier;
    private final CaseJdbc cases;

    public CaseService(Rule rules, CaseNotifier notifier, CaseJdbc cases) {
        this.rules = rules;
        this.notifier = notifier;
        this.cases = cases;
    }

    public CaseStatus move(CaseId id, CaseStatus from, CaseStatus to) {
        rules.check(from, to);          // forbidden move throws here, notifier is never called
        notifier.caseMoved(id, to);     // only after the move is allowed
        return to;
    }

    @Transactional
    public void register(CaseId id, CaseNumber number, String title) {
        try {
            cases.insert(id, number, new CaseStatus.New(), title);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCase(number);    // rethrow, do not swallow
        }
    }

    @Transactional
    public void insertTwice(CaseNumber number) {
        cases.insert(CaseId.newId(), number, new CaseStatus.New(), "Card blocked");
        try {
            cases.insert(CaseId.newId(), number, new CaseStatus.New(), "Card blocked again");
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCase(number);    // unchecked -> rollback of the first INSERT too
        }
    }
}