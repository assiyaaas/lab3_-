# Product

Bank Customer Case Management is a simple system for tracking customer cases in a bank. It helps track a case from creation to resolution.

# Core item

The core item is a Case.

Each Case has a CaseId and a CaseStatus.

# Status table

| From | To | Allowed |
|---|---|---|
| New | InProgress | Yes |
| InProgress | Resolved | Yes |
| New | Resolved | No |
| Resolved | New | No |

# Forbidden — why

- New → Resolved is forbidden because a new case must be processed before it can be resolved.
- Resolved → New is forbidden because a resolved case cannot directly return to the new state.

# Rules

`Rule` is a plain Java interface in `domain`. Two implementations work together through `RuleChain`:

- `CaseStopRule`: stop-factor, runs first. A resolved case cannot be changed in any direction.
- `TransitionRule`: the status table above (delegates to `CasePolicy.move`).

The chain is wired with a `@Bean` in `config/RuleConfig`. Spring injects `Rule` into `CaseService`.

# Spring Boot integration

## Package diagram

```
  dto          client        handler         config
  (JSON later) (HTTP later)  (HTTP week 9)   BankCaseManagementApplication
                                             RuleConfig
                                             @Service CaseService
       \            \            /                |
        \            \          /           injects Rule
         \            \        /
                     domain
          CaseId  CaseStatus  CasePolicy
          Rule  + CaseStopRule, TransitionRule (RuleChain)
                     (no Spring)
```

Arrows point inward. Domain never imports `org.springframework`.

## Maven modules

```
bank-case-management/   packaging pom: parent + <modules>
  domain/               CaseId, CaseStatus, CasePolicy, Rule, DuplicateCase
                        dependencies: junit-jupiter (test) only
  app/                  config, persistence, client, dto, handler
                        depends on domain + Spring Boot, JPA, Flyway, PostgreSQL
```

The boundary is checked at compile time: an `org.springframework` import in `domain` does not compile,
because the `domain` module has no Spring dependency. `DomainHasNoSpringTest` also checks for
`java.sql` and `jakarta.persistence`.

# Running

```
mvn -q verify                          # both modules, all tests (the hand-in command)
mvn -pl domain -q test                 # only the domain module
mvn -q install -DskipTests             # once, so app can find the domain jar
mvn -pl app spring-boot:run            # starts the application from the app module
```
# Database: Flyway and Hibernate (Lab 3)

The project uses PostgreSQL to store bank customer cases.
The database connection is configured in `application.properties`.

## Database table

The table is named `bank_case` and contains:

| Column | Type | Purpose |
|---|---|---|
| `id` | UUID | Primary key |
| `business_key` | TEXT | Unique business number of a case |
| `status` | TEXT | Case status, restricted by a CHECK constraint |
| `title` | TEXT | Case title |
| `created_at` | TIMESTAMPTZ | Creation time, defaults to `now()` |

The `business_key` column is required and unique.
The `status` column accepts `New`, `InProgress`, and `Resolved`.

The schema is managed by Flyway: migrations live in `src/main/resources/db/migration`
(`V1__create_bank_case.sql`) and are applied at startup.
Hibernate runs with `ddl-auto=validate`: it never changes the schema, it only checks that the entities match it.

## JPA persistence

The `persistence` package contains:

- `CaseEntity` – JPA entity mapped to `bank_case`; `status` is a string with the same tokens as the CHECK.
- `CaseMapper` – translates the sealed `CaseStatus` to the column text and back; unknown text throws.
- `CaseJpa` – uses `EntityManager`: `persist` for the INSERT, a JPQL count by business key, and `flush`.

`CaseService` flushes inside its `try`, so the UNIQUE violation (23505) becomes `DuplicateCase`
inside the `@Transactional` method and the transaction rolls back.

The domain package remains independent of Spring, JPA and JDBC.

## Transaction isolation

`CaseService.register` and `insertTwice` use `@Transactional(isolation = Isolation.READ_COMMITTED)`.
This is the PostgreSQL default, written out so the choice is visible:

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| READ_UNCOMMITTED (PostgreSQL treats it as READ_COMMITTED) | no | possible | possible |
| **READ_COMMITTED** (used here) | no | possible | possible |
| REPEATABLE_READ | no | no | no in PostgreSQL |
| SERIALIZABLE | no | no | no |

Two requests that register the same business number are not stopped by the isolation level:
the `UNIQUE` constraint on `business_key` rejects the second INSERT, and the service turns it into `DuplicateCase`.

## Resetting the local database

Flyway must create the table from `V1`. If `css` already has the table, recreate it
and boot again. Connect to the `postgres` database as the `postgres` superuser
(the `css` role cannot create databases):

```sql
DROP DATABASE IF EXISTS css WITH (FORCE);
CREATE DATABASE css OWNER css;
```

## Tests

| Kind | Class | What it shows |
|---|---|---|
| Unit, `@ParameterizedTest` + `@CsvSource` | `domain` / `CasePolicyTest` | The README status table, row by row |
| `@Nested`, `assertAll` | `domain` / `CasePolicyTest` | Groups `Transitions`, `CaseId`, `CaseNumber`; several checks reported together |
| Unit with Mockito | `app` / `CaseServiceNotifierTest` | `CaseNotifier` port mocked: `times(1)` + `ArgumentCaptor` checks the same id was sent, `verifyNoMoreInteractions`; never notified on a forbidden move |
| Slice, `@DataJpaTest` | `app` / `persistence/CaseJpaSliceTest` | Only the JPA layer on PostgreSQL (no `CaseService`, `Rule`, `CaseNotifier` in the context): persist + read back, `UNIQUE` and `CHECK` enforced by the database. Each test is rolled back, so it never asserts committed counts: that is `CaseDatabaseTest` |
| Integration, `@SpringBootTest` | `app` / `CaseServiceTest`, `CaseDatabaseTest` | One context test (`assertNotNull` + the rule is wired); on PostgreSQL `insertTwice` -> count 0, second `register` -> count 1, no `@Transactional` on the test |
| Architecture | `domain` / `DomainHasNoSpringTest` | `domain` has no Spring, JDBC or JPA imports |

## Running verification

Run the project verification with the Maven Wrapper:

```bash
./mvnw -q verify

```
