package com.example.bankcase;

import com.example.bankcase.domain.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = com.example.bankcase.config.BankCaseManagementApplication.class)
class CaseServiceTest {

    @Autowired
    private CaseService caseService;

    @Test
    void springInjectsCaseService() {
        CaseStatus result = caseService.move(
                new CaseStatus.New(),
                new CaseStatus.InProgress()
        );

        assertEquals(new CaseStatus.InProgress(), result);
    }
}