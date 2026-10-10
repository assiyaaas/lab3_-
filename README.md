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

Arrows point inward. Domain never imports `org.springframework`; `DomainHasNoSpringTest` fails the build if it does.

# Running

```
mvn -q verify          # tests
mvn spring-boot:run    # starts the application
```
# Database and JDBC (Lab 3)

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
The `status` column accepts `NEW`, `IN_PROGRESS`, and `RESOLVED`.

The SQL schema is stored in `src/main/resources/db/schema.sql`.
Spring Boot is configured to initialize the schema at startup.

## JDBC repository

`CaseJdbc` is located in the `persistence` package.
It uses Spring's `JdbcTemplate` to:

- Insert a case using parameterized SQL placeholders.
- Count cases by their business number.

The domain package remains independent of Spring and JDBC.

## Running verification

Run the project verification with the Maven Wrapper:

```bash
./mvnw -q verify

```
