package com.example.bankcase.persistence;

import com.example.bankcase.config.BankCaseManagementApplication;
import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNotifier;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseStatus;
import com.example.bankcase.domain.Rule;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Slice test: only the JPA layer (EntityManager, Hibernate, Flyway, CaseJpa).
 * No CaseService, no rules, no notifier in the context.
 * Runs on the real PostgreSQL (no embedded database replacement).
 * @DataJpaTest is @Transactional: every test is rolled back, the table stays empty.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BankCaseManagementApplication.class)
@Import(CaseJpa.class)
// the slice does not translate exceptions by itself; the full application does, and CaseService relies on it
@ImportAutoConfiguration(PersistenceExceptionTranslationAutoConfiguration.class)
class CaseJpaSliceTest {

    @Autowired
    private CaseJpa cases;

    @Autowired
    private EntityManager em;

    @Autowired
    private ApplicationContext context;

    private final CaseNumber number = new CaseNumber("CASE-SLICE-1");

    @Test
    void sliceHasNoServiceLayer() {
        assertAll("beans outside the JPA slice",
                () -> assertEquals(0, context.getBeanNamesForType(CaseService.class).length),
                () -> assertEquals(0, context.getBeanNamesForType(Rule.class).length),
                () -> assertEquals(0, context.getBeanNamesForType(CaseNotifier.class).length));
    }

    @Test
    void persistedRowIsCountedAndReadsBack() {
        CaseId id = CaseId.newId();
        cases.insert(CaseMapper.toEntity(id, number, new CaseStatus.New(), "Card blocked"));
        cases.flush();
        em.clear(); // forget the cached object, read the row from the database

        CaseEntity row = em.find(CaseEntity.class, id.value());

        assertAll("row from bank_case",
                () -> assertEquals(1, cases.count(number)),
                () -> assertEquals(number.value(), row.getBusinessKey()),
                () -> assertEquals("New", row.getStatus()),
                () -> assertEquals(new CaseStatus.New(), CaseMapper.fromText(row.getStatus())),
                () -> assertNotNull(row.getCreatedAt(), "created_at comes from DEFAULT now()"));
    }

    @Test
    void uniqueBusinessKeyIsEnforcedByPostgres() {
        cases.insert(CaseMapper.toEntity(CaseId.newId(), number, new CaseStatus.New(), "first"));
        cases.insert(CaseMapper.toEntity(CaseId.newId(), number, new CaseStatus.New(), "second"));

        assertThrows(DataIntegrityViolationException.class, () -> cases.flush());
    }

    @Test
    void checkConstraintRejectsUnknownStatus() {
        CaseEntity row = CaseMapper.toEntity(CaseId.newId(), number, new CaseStatus.New(), "bad status");
        row.setStatus("BANANA"); // Hibernate would store it; the CHECK in V1 stops it

        cases.insert(row);

        assertThrows(DataIntegrityViolationException.class, () -> cases.flush());
    }
}
