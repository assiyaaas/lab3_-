Title: CSS-?? JPA and Flyway

<!-- Replace CSS-?? with the real story ID from your Jira board -->

## What
- `spring-boot-starter-jdbc` replaced by `spring-boot-starter-data-jpa` (Hibernate); `flyway-core` + `flyway-database-postgresql` added
- `db/schema.sql` moved to `src/main/resources/db/migration/V1__create_bank_case.sql`: surrogate `id uuid`, `business_key` UNIQUE NOT NULL, CHECK on status
- `persistence/CaseEntity`: `@Entity` for `bank_case`, `@Id` is the UUID from `CaseId` (no `@GeneratedValue`), `created_at` filled by the database default
- `persistence/CaseMapper`: sealed `CaseStatus` <-> status text
- `persistence/CaseJpa`: `EntityManager.persist`, JPQL count, `flush`
- `CaseService.register` and `insertTwice`: `@Transactional`, flush inside `try`, `DataIntegrityViolationException` -> `DuplicateCase`
- `application.properties`: `ddl-auto=validate`, `open-in-view=false`, Flyway defaults (`baseline-on-migrate=false`, `out-of-order=false`)
- `CaseDatabaseTest`: `DELETE FROM bank_case` in `@BeforeEach` (no DROP, no schema.sql)
- `DomainHasNoSpringTest` also checks for `jakarta.persistence`
- README: Flyway + JPA section

## Checklist
- [ ] Story ID in the title
- [ ] Same product as Labs 1 and 2
- [ ] Week 4 tests still green (status table + one mocked port)
- [ ] V1 has a surrogate id, a `UNIQUE` business key, `CHECK` on statuses
- [ ] Empty database (`css` recreated) -> boot -> table exists, `flyway_schema_history` has version 1
- [ ] `ddl-auto` is `validate`; the entity lives in `persistence`
- [ ] `secondStatementRollsBack` -> count is 0
- [ ] `secondRequestKeepsTheFirst` -> count is 1
- [ ] `domain` has no `jakarta.persistence`, no `java.sql`, no `org.springframework`
- [ ] `mvn -q verify` is green against PostgreSQL
- [ ] No REST controller, no cloud passwords, no `target/` in git