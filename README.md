# Distributed Wallet Application — Saga Orchestration over Sharded MySQL

A production-grade, distributed financial wallet system built with **Spring Boot** and **Apache ShardingSphere**. The system manages peer-to-peer money transfers across horizontally sharded MySQL databases using the **Saga pattern** with full orchestration, persistent state, and automatic compensation.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Core Architecture](#core-architecture)
3. [Saga Pattern — Orchestration Style](#saga-pattern--orchestration-style)
4. [Saga Lifecycle & State Machine](#saga-lifecycle--state-machine)
5. [Horizontal Database Sharding](#horizontal-database-sharding)
6. [Pessimistic Locking & Concurrency Control](#pessimistic-locking--concurrency-control)
7. [Saga Context Persistence](#saga-context-persistence)
8. [Ordered Compensation](#ordered-compensation)
9. [Entity Auditing & Immutability Constraints](#entity-auditing--immutability-constraints)
10. [REST API Reference](#rest-api-reference)
11. [Domain Model](#domain-model)
12. [Technology Stack](#technology-stack)
13. [Setup & Configuration](#setup--configuration)
14. [Planned Improvements](#planned-improvements)

---

## System Overview

This application models a **digital wallet ecosystem** where each registered user owns exactly one wallet. Users can top up their wallets (credit), withdraw from them (debit), and transfer funds to other users. Every transfer is coordinated as a **distributed transaction** using the Saga pattern, ensuring that no money is created or destroyed even when a partial failure occurs mid-transfer.

The system is designed to scale horizontally: all persistent data — users, wallets, transactions, and saga bookkeeping tables — is distributed across multiple physical database shards using **Apache ShardingSphere** as a transparent sharding middleware layer.

---

## Core Architecture

The application follows a **layered architecture** with clear separation of concerns:

- **Presentation Layer (REST Controllers):** Accepts inbound HTTP requests, validates request payloads using Bean Validation (`@Valid`), delegates to the service layer, and serialises responses through **Data Transfer Objects (DTOs)**.
- **Adapter Layer:** Stateless mapper components that convert between domain entities and DTOs, keeping the domain model free from serialisation concerns.
- **Service Layer:** Encapsulates all business logic. The saga orchestration logic is entirely separated from the plain wallet and user services, following the **Single Responsibility Principle**.
- **Repository Layer:** Spring Data JPA repositories that issue JPQL queries against the logical schema; Apache ShardingSphere intercepts and routes each query to the correct physical shard transparently.
- **Domain Model:** JPA entities annotated with persistence metadata. A shared `BaseModel` provides common auditing fields (`id`, `createdAt`, `updatedAt`) to all entities via `@MappedSuperclass`.

---

## Saga Pattern — Orchestration Style

### Why Saga?

Distributed systems cannot use a traditional two-phase commit (2PC) protocol across independent databases without tight coupling and availability risk. The **Saga pattern** breaks a long-running distributed transaction into a sequence of individually atomic local transactions. Each local transaction updates a single resource and publishes events or signals to trigger the next step.

### Orchestration vs. Choreography

This system uses the **Orchestration variant** of the Saga pattern. A central **Saga Orchestrator** (`SagaOrchestratorImpl`) holds the authoritative execution plan and explicitly commands each participant step to execute or compensate. This differs from Choreography, where each participant reacts autonomously to domain events. Orchestration was chosen because it provides:

- A single, observable point of control for the entire distributed flow.
- Straightforward debugging: the orchestrator's database records act as a distributed transaction log.
- Deterministic step ordering without complex event routing.

### Transfer Money Saga — Step Sequence

When a transfer is requested, the orchestrator executes the following **saga steps** in strict sequential order:

| Order | Step Name | Forward Action | Compensating Action |
|-------|-----------|----------------|---------------------|
| 1 | `DEBIT_SOURCE_WALLET_STEP` | Acquires a **pessimistic write lock** on the sender's wallet and debits the specified amount. Snapshots the wallet balance before and after the operation into the **Saga Context**. | Credits the sender's wallet by the same amount, effectively reversing the debit. |
| 2 | `CREDIT_DESTINATION_WALLET_STEP` | Acquires a **pessimistic write lock** on the recipient's wallet and credits the specified amount. Snapshots balance changes into the Saga Context. | Debits the recipient's wallet by the same amount, reversing the credit. |
| 3 | `UPDATE_TRANSACTION_STATUS_STEP` | Reads the target status from the Saga Context (`destinationTransactionStatus`) and persists it on the Transaction record. | Restores the Transaction record to its original status prior to this step's execution (`originalTransactionStatus`). |

If **any step fails**, the orchestrator immediately calls `failSaga()`, which triggers `compensateSaga()`. Compensation iterates through all previously **completed** steps in **reverse chronological order** (ordered by timestamp descending) and calls each step's `compensate()` method, unwinding changes in the correct order.

### ISagaStep Contract

Every saga participant implements the `ISagaStep` interface, which mandates two methods:

- **`execute(SagaContext)`** — Performs the forward business action and returns `true` on success or `false` on logical failure.
- **`compensate(SagaContext)`** — Undoes the effects of a prior `execute` call and returns `true` on success.

The `SagaContext` object is passed through every step, enabling steps to read inputs written by the orchestrator and to publish outputs (balance snapshots, status values) that downstream steps or the compensation flow can consume.

### SagaStepFactory

The `SagaStepFactory` acts as a **registry** of all `ISagaStep` implementations, keyed by their canonical step name (an enum value of `SagaStepType`). The orchestrator resolves the correct step implementation at runtime using this factory, making it trivial to register new saga steps without modifying orchestration logic.

---

## Saga Lifecycle & State Machine

### SagaInstance States

A `SagaInstance` record is created for every transfer and transitions through the following states:

```
STARTED → RUNNING → COMPLETED
                  ↘ FAILED → COMPENSATING → COMPENSATED
```

| State | Meaning |
|-------|---------|
| `STARTED` | The saga has been initialised; no steps have executed yet. |
| `RUNNING` | At least one step has completed successfully; execution is in progress. |
| `COMPLETED` | All steps executed successfully; the transfer is finalised. |
| `FAILED` | One or more steps failed; compensation has been triggered. |
| `COMPENSATING` | The orchestrator is actively executing compensation steps. |
| `COMPENSATED` | All completed steps have been successfully compensated; the system is restored to its pre-transfer state. |

### SagaStep States

Each individual step persists its own lifecycle record (`SagaStep`) in the database:

| State | Meaning |
|-------|---------|
| `PENDING` | Step is registered but not yet started. |
| `RUNNING` | Step execution is in progress. |
| `COMPLETED` | Step executed successfully. |
| `FAILED` | Step execution threw an exception or returned `false`. |
| `COMPENSATING` | Step compensation is in progress. |
| `COMPENSATED` | Step has been successfully rolled back. |
| `CANCELLED` | Step was skipped because a prior step failed before this one ran. |

---

## Horizontal Database Sharding

### Apache ShardingSphere

Apache ShardingSphere acts as a **JDBC proxy layer** that intercepts all SQL statements issued by Spring Data JPA. The application connects to a single logical data source while ShardingSphere routes each query to the appropriate physical shard based on the **sharding key** embedded in the query.

### Sharding Strategy

Two physical MySQL databases (`realshard1`, `realshard2`) serve as the shard nodes. Both are accessed via **HikariCP** connection pools for optimal connection reuse and latency.

All sharded tables use an **INLINE sharding algorithm**, which evaluates a Groovy expression against the sharding column at query time:

| Logical Table | Sharding Column | Algorithm Expression | Rationale |
|--------------|-----------------|---------------------|-----------|
| `user` | `id` | `realshard${id % 2 + 1}` | Evenly distributes users across both shards. |
| `wallet` | `user_id` | `realshard${user_id % 2 + 1}` | Co-locates each wallet with its owning user on the same shard, eliminating cross-shard joins for user–wallet lookups. |
| `transaction` | `id` | `realshard${id % 2 + 1}` | Distributes transaction records evenly. |
| `saga_instance` | `id` | `realshard${id % 2 + 1}` | Distributes saga bookkeeping records evenly. |
| `saga_step` | `id` | `realshard${id % 2 + 1}` | Distributes step-level audit records evenly. |

### Snowflake ID Generation

All primary keys are generated using the **Snowflake algorithm** (configured as the ShardingSphere key generator). Snowflake produces **64-bit, time-ordered, globally unique identifiers** without requiring a centralised sequence generator. This is critical in a sharded environment because database `AUTO_INCREMENT` sequences are local to each shard and would produce collisions across shards if used naively.

### Sharding Key Immutability

The `user_id` column on the `wallet` table is declared `updatable = false` at the JPA level. Because `user_id` is the sharding key, mutating it after insertion would cause ShardingSphere to route update queries to the wrong shard, resulting in silent data corruption. Making it immutable prevents this class of bug entirely.

---

## Pessimistic Locking & Concurrency Control

Wallet balance mutations (both within the saga and via direct debit/credit endpoints) use **pessimistic write locking** (`SELECT ... FOR UPDATE`) via JPA's `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

### Why Pessimistic Locking?

In a concurrent financial system, two transfers involving the same wallet could read the same balance value simultaneously (a **lost update anomaly**). Optimistic locking (version-based) would detect this collision only at commit time and throw an exception, requiring the caller to retry. Pessimistic locking prevents the conflict entirely by serialising access to the wallet row at the database level, guaranteeing that only one transaction can modify a wallet's balance at any given moment.

### Application

- **Saga steps (`DebitSourceWalletStep`, `CreditDestinationWalletStep`):** Both acquire the lock before reading the balance, ensuring the balance read is consistent with the row that will be written.
- **Direct wallet endpoints (`WalletService.debit`, `WalletService.credit`):** Also acquire the pessimistic write lock for the same consistency guarantee, even outside the saga flow.

---

## Saga Context Persistence

The `SagaContext` is a key-value map that carries all data required by saga steps and their compensation handlers. After each step executes (forward or compensating), the orchestrator **serialises the updated `SagaContext` to JSON** using Jackson's `ObjectMapper` and persists it inside the `SagaInstance` record's `context` column.

This design has two critical benefits:

1. **Crash Recovery:** If the application process dies mid-saga, the persisted context contains enough information (source user ID, destination user ID, amount, balance snapshots, transaction ID) to resume or compensate from the exact point of failure without any in-memory state.
2. **Audit Trail:** The context column serves as a detailed, immutable audit log of every balance change and state transition that occurred during a transfer, invaluable for support and forensic debugging.

---

## Ordered Compensation

Compensation is not simply a reversal of the step list; it must run in **strictly reverse chronological order** to maintain logical correctness. For example, if a debit was executed at T1 and a credit at T2, the credit must be compensated before the debit.

The system achieves this by querying the `SagaStep` table for all `COMPLETED` steps belonging to a given `SagaInstance`, ordered by their `createdAt` timestamp in **descending order**. The orchestrator then iterates through this ordered list and calls each step's `compensate()` method.

---

## Entity Auditing & Immutability Constraints

### Spring Data JPA Auditing

All domain entities extend `BaseModel`, which is annotated with `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`. Spring Data JPA automatically populates:

- **`createdAt`** — Set once on initial persistence, declared `updatable = false` to prevent accidental mutation.
- **`updatedAt`** — Automatically refreshed on every update operation, providing an accurate last-modified timestamp.

These timestamps are the source of truth for ordering saga step compensation.

### Database-Level Balance Constraint

A `CHECK (balance >= 0)` constraint is applied at the database schema level on the `wallet` table. This is a **defence-in-depth** measure: even if application-level validation fails to prevent a negative balance (e.g., due to a bug in compensation logic), the database will reject the write and surface an error. Application-level validation within `Wallet.debit()` provides the first line of defence; the database constraint is the last.

---

## REST API Reference

All endpoints follow REST conventions and return JSON payloads. The base path for all endpoints is `/api/v1`.

### User Endpoints — `/api/v1/user`

| Method | Path | Description | Request Body | Success Status |
|--------|------|-------------|--------------|----------------|
| `POST` | `/api/v1/user` | Register a new user. | `UserRequestDTO` | `201 Created` |
| `GET` | `/api/v1/user/id/{id}` | Retrieve a user by their unique ID. | — | `200 OK` |
| `GET` | `/api/v1/user?name={name}` | Retrieve a user by name (query parameter). | — | `200 OK` |

### Wallet Endpoints — `/api/v1/wallet`

| Method | Path | Description | Request Body | Success Status |
|--------|------|-------------|--------------|----------------|
| `POST` | `/api/v1/wallet` | Create a wallet for an existing user. | `WalletRequestDTO` | `201 Created` |
| `GET` | `/api/v1/wallet/user/{userId}` | Retrieve a user's full wallet details. | — | `200 OK` |
| `GET` | `/api/v1/wallet/user/{userId}/balance` | Retrieve a user's current balance. | — | `200 OK` |
| `POST` | `/api/v1/wallet/user/{userId}/debit` | Directly debit an amount from a wallet. | `DebitWalletRequestDTO` | `200 OK` |
| `POST` | `/api/v1/wallet/user/{userId}/credit` | Directly credit an amount to a wallet. | `CreditWalletRequestDTO` | `200 OK` |

### Transaction Endpoints — `/api/v1/transaction`

| Method | Path | Description | Request Body | Success Status |
|--------|------|-------------|--------------|----------------|
| `POST` | `/api/v1/transaction` | Initiate a peer-to-peer transfer. Creates a transaction record, instantiates a saga, and executes the full transfer pipeline synchronously. Returns the `sagaInstanceId`. | `TransactionRequestDTO` | `201 Created` |

The `TransactionRequestDTO` carries: `fromUserId`, `toUserId`, `amount`, and `description`.

---

## Domain Model

### User

Represents a registered participant in the wallet system. Each user is uniquely identified by a Snowflake-generated `id` and is co-located with their wallet on the same database shard via the shared `user_id` sharding key relationship.

### Wallet

Represents the financial account of a single user. A wallet holds a `balance` (stored as `BigDecimal` for decimal precision, avoiding floating-point rounding errors common in financial systems), an `isActive` flag, and an immutable `userId` reference. The wallet's domain object enforces business invariants directly: `debit()` rejects amounts that exceed the current balance or are non-positive, and `credit()` rejects non-positive amounts.

### Transaction

An immutable record of a transfer intent. Core fields (`fromUserId`, `toUserId`, `amount`, `type`, `description`) are all declared `updatable = false` to preserve the integrity of the historical ledger. Only the `status` field is mutable, transitioning from `PENDING` to `SUCCESS` (or staying at `PENDING` if compensation occurs). The `sagaInstanceId` links the transaction to its governing saga.

**TransactionStatus values:** `PENDING`, `SUCCESS`
**TransactionType values:** `TRANSFER`

### SagaInstance

The orchestrator's primary bookkeeping record. Persists the current `SagaStatus`, the `currentStep` name, and the full serialised `SagaContext` JSON. One `SagaInstance` exists per transfer.

### SagaStep

A granular audit record for each step execution within a saga. Stores the `sagaInstanceId`, `stepName`, `StepStatus`, an optional `errorMessage`, and a `stepData` JSON blob. One `SagaStep` row is created per step per saga.

---

## Technology Stack

| Technology | Role |
|-----------|------|
| **Spring Boot 3.x** | Application framework, dependency injection, auto-configuration |
| **Spring Data JPA** | ORM abstraction over Hibernate; repository pattern implementation |
| **Hibernate** | JPA provider; SQL generation, session management |
| **Apache ShardingSphere (JDBC)** | Transparent horizontal sharding middleware |
| **MySQL** | Physical relational database (two shard instances) |
| **HikariCP** | High-performance JDBC connection pooling |
| **Jackson (FasterXML)** | JSON serialisation of `SagaContext` and HTTP response bodies |
| **Lombok** | Compile-time boilerplate reduction (`@Builder`, `@Getter`, `@Slf4j`, etc.) |
| **Snowflake Algorithm** | Distributed, time-ordered unique ID generation |
| **Bean Validation (Jakarta)** | Declarative request payload validation via `@Valid` |
| **SLF4J / Logback** | Structured application logging |
| **Gradle** | Build automation and dependency management |

---

## Setup & Configuration

### Prerequisites

- Java 21+
- MySQL 8.0+ (two separate schemas: `realshard1`, `realshard2`)
- Gradle 8+

### Database Preparation

Create two schemas in MySQL and ensure both contain identical table definitions for `user`, `wallet`, `transaction`, `saga_instance`, and `saga_step`. All tables require:
- A `CHECK (balance >= 0)` constraint on `wallet.balance`.
- A `json` column type for `saga_instance.context` and `saga_step.step_data`.

### ShardingSphere Configuration

The `sharding.yml` file defines the two data sources and all sharding rules. Update the `jdbcUrl`, `username`, and `password` for each shard to match your MySQL environment before starting the application.

### Running the Application

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080` by default. Set `props.sql-show: true` in `sharding.yml` (already enabled) to log every SQL statement with its resolved physical shard target — invaluable for verifying that routing is correct during development.

---

## Planned Improvements

### Parallel Compensation
Execute independent compensation steps concurrently using `CompletableFuture` or Java 21 virtual threads. This reduces total rollback latency when multiple steps can be compensated simultaneously without ordering dependencies.

### Idempotency Keys
Assign a unique idempotency key to each saga step execution. Before re-executing a step (e.g., on a retry), the orchestrator would check whether that step has already been successfully committed, preventing **duplicate debits or credits** in at-least-once delivery scenarios.

### Retry with Exponential Backoff
Integrate **Resilience4j** `@Retry` annotations on step `execute()` calls with configurable exponential backoff and jitter. Transient failures (network blips, brief database unavailability) would be automatically retried without triggering full compensation.

### Circuit Breaker
Wrap calls to downstream dependencies with a **Resilience4j Circuit Breaker**. When a dependency's failure rate exceeds a threshold, the circuit opens and subsequent calls fail fast rather than accumulating thread pool saturation.

### Saga Timeout
Implement a scheduled job or `@Scheduled` monitor that detects sagas stuck in `RUNNING` or `COMPENSATING` for longer than a configurable TTL and marks them as `FAILED`, triggering compensation automatically.

### Global Exception Handling
Add a `@ControllerAdvice` class to intercept all uncaught exceptions across every controller and return consistent, machine-readable `ProblemDetail` (RFC 9457) error responses, rather than relying on Spring Boot's default error page.

### Observability
Integrate **Micrometer** with a metrics backend (e.g., Prometheus + Grafana) to expose saga throughput, step failure rates, compensation latency, and wallet balance change histograms as production-grade metrics.

### Event Sourcing for Audit Trails
Instead of (or in addition to) the `SagaContext` JSON blob, model all balance mutations as immutable domain events stored in an append-only event log. This enables point-in-time balance reconstruction and a tamper-evident audit trail.
