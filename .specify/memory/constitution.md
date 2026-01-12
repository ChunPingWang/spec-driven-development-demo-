<!--
  SYNC IMPACT REPORT
  ====================
  Version change: 0.0.0 → 1.0.0 (Initial constitution adoption)

  Modified principles: N/A (Initial version)

  Added sections:
  - Core Principles (6 principles)
  - Architecture Constraints
  - Development Workflow
  - Governance

  Removed sections: N/A (Initial version)

  Templates requiring updates:
  - .specify/templates/plan-template.md: ✅ Compatible (Constitution Check section exists)
  - .specify/templates/spec-template.md: ✅ Compatible (BDD scenarios supported)
  - .specify/templates/tasks-template.md: ✅ Compatible (TDD workflow supported)

  Follow-up TODOs: None
-->

# SDD Demo Constitution

## Core Principles

### I. Code Quality First

All code MUST meet high quality standards. This includes:

- Clean, readable, and maintainable code
- Consistent coding style and conventions
- Meaningful naming for variables, functions, and classes
- Small, focused functions with single responsibilities
- Comprehensive documentation where behavior is not self-evident

**Rationale**: Quality code reduces technical debt, eases maintenance, and enables sustainable development velocity.

### II. Test-Driven Development (TDD)

Development MUST follow the Red-Green-Refactor cycle:

1. **Red**: Write a failing test that defines expected behavior
2. **Green**: Write minimal code to make the test pass
3. **Refactor**: Improve code structure while keeping tests green

Tests MUST be written before implementation. No production code is written without a failing test first.

**Rationale**: TDD ensures testable design, reduces defects, and provides living documentation of system behavior.

### III. Behavior-Driven Development (BDD)

User-facing features MUST be specified using BDD scenarios:

- **Given** [initial context/state]
- **When** [action/event occurs]
- **Then** [expected outcome]

Acceptance criteria MUST be expressed in this format to ensure clear, testable requirements.

**Rationale**: BDD bridges communication between technical and non-technical stakeholders, ensuring features meet actual user needs.

### IV. Domain-Driven Design (DDD)

The codebase MUST reflect the business domain through:

- Ubiquitous language shared between developers and domain experts
- Bounded contexts with clear boundaries
- Domain models that encapsulate business logic
- Entities, value objects, and aggregates as core building blocks
- Domain events for cross-context communication

**Rationale**: DDD ensures the software model aligns with business reality, reducing translation errors and improving maintainability.

### V. SOLID Principles

All object-oriented code MUST adhere to SOLID principles:

- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes MUST be substitutable for base types
- **I**nterface Segregation: Prefer specific interfaces over general ones
- **D**ependency Inversion: Depend on abstractions, not concretions

**Rationale**: SOLID principles produce flexible, maintainable, and testable code architectures.

### VI. Hexagonal Architecture

The system MUST follow hexagonal (ports and adapters) architecture:

- **Domain Layer** (innermost): Business logic and domain models
- **Application Layer**: Use cases and application services
- **Infrastructure Layer** (outermost): Frameworks, databases, external services

Layer access rules:
- Infrastructure MAY directly use Application and Domain layers
- Application and Domain layers MUST access Infrastructure only through interfaces (ports)
- Data transfer between layers MUST use mappers for transformation
- No framework dependencies in Domain or Application layers

**Rationale**: Hexagonal architecture isolates business logic from technical concerns, enabling technology changes without domain impact.

## Architecture Constraints

### Dependency Flow

Dependencies MUST flow inward only:

```
Infrastructure → Application → Domain
     ↓               ↓           ↓
   (outer)       (middle)     (inner)
```

### Interface Requirements

- All external dependencies MUST be abstracted behind interfaces
- Interfaces MUST be defined in the layer that uses them (Application or Domain)
- Implementations MUST reside in Infrastructure layer

### Mapper Requirements

Data crossing layer boundaries MUST be transformed via mappers:

- DTOs for API/external communication
- Domain models for business logic
- Persistence models for database operations

No direct entity exposure to external layers.

## Development Workflow

### Test Standards

- Unit tests for domain and application logic (isolated, fast)
- Integration tests for infrastructure adapters
- Contract tests for external API boundaries
- All tests MUST be deterministic and repeatable

### Code Review Gates

All changes MUST pass:

1. All tests green
2. No SOLID violations
3. Layer boundaries respected
4. Mappers used for cross-layer data transfer
5. Domain logic free of infrastructure dependencies

### Definition of Done

A feature is complete when:

- All acceptance scenarios (BDD) pass
- Unit test coverage for new domain/application code
- Integration tests for new infrastructure code
- Code review approved
- Documentation updated (if behavior changed)

## Governance

This constitution supersedes all other development practices. Amendments require:

1. Written proposal with rationale
2. Impact assessment on existing code
3. Migration plan if breaking changes
4. Version update following semantic versioning

All pull requests and code reviews MUST verify compliance with these principles. Complexity beyond what is specified here MUST be explicitly justified and documented.

**Version**: 1.0.0 | **Ratified**: 2026-01-12 | **Last Amended**: 2026-01-12
