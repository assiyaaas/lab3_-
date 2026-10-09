package com.example.bankcase;

import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.example.bankcase.config.BankCaseManagementApplication.class)
class CaseServiceTest {

    @Autowired
    private CaseService caseService;

    private final CaseId id = CaseId.newId();

    @Test
    void springInjectsCaseService() {
        CaseStatus result = caseService.move(
                id,
                new CaseStatus.New(),
                new CaseStatus.InProgress()
        );

        assertEquals(new CaseStatus.InProgress(), result);
    }

    @Test
    void resolvedCaseIsStoppedByStopRule() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> caseService.move(id, new CaseStatus.Resolved(), new CaseStatus.InProgress()));
        assertTrue(e.getMessage().contains("resolved case cannot be changed"));
    }
}