package com.example.bankcase.config;

import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.persistence.CaseJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.bankcase.persistence.CaseMapper.toEntity;

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

    @Transactional
    public void register(CaseId id, CaseNumber number, String title) {
        try {
            cases.insert(toEntity(id, number, new CaseStatus.New(), title));
            cases.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCase(number);
        }
    }

    @Transactional
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
