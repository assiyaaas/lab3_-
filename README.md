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