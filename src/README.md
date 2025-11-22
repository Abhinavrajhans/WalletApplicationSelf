# Wallet Application - Saga Pattern Implementation

## Project Overview
A distributed wallet system implementing the Saga pattern for managing financial transactions across sharded databases using Spring Boot and Apache ShardingSphere.

---

## Improvements Implemented

### 1. **Saga Context Persistence**
- Save updated `SagaContext` to database after each step execution and compensation
- Ensures state consistency across distributed operations
- Critical for recovery and debugging failed sagas

### 2. **Ordered Compensation**
- Order completed saga steps by timestamp (DESC) during compensation
- Ensures steps are rolled back in reverse order of execution
- Maintains transactional consistency during failure scenarios

### 3. **Entity Immutability Constraints**
- Set `updatable = false` for `userId` in Wallet entity
- Prevents accidental modification of sharding keys
- Avoids ShardingSphere routing errors during updates

### 4. **Pessimistic Locking for Wallet Operations**
- Use `findByUserIdWithLock()` for all credit/debit operations
- Prevents race conditions and ensures balance consistency
- Applied in both WalletService and Saga steps

### 5. **Consistent Transaction Model**
- Changed `fromWalletId`/`toWalletId` to `fromUserId`/`toUserId`
- Aligns with sharding strategy (user-based partitioning)
- Simplifies wallet lookup and reduces join complexity

### 6. **Database Constraints**
- Added check constraint: `CHECK (balance >= 0)` on wallet table
- Provides database-level protection against negative balances
- Complements application-level validations

---

## Future Improvements

### 1. **Parallel Compensation**
- Execute compensation steps concurrently using `CompletableFuture` or virtual threads
- Reduce saga rollback time
- Maintain order guarantees where necessary

### 2. **Retry Mechanism with Resilience4j**
- Add `@Retry` annotations for transient failures
- Configure exponential backoff strategies
- Implement circuit breakers for external dependencies

### 3. **Thread-Safe Saga Context**
- Use `ConcurrentHashMap` for saga context data
- Add synchronization for parallel step execution
- Consider immutable context snapshots per step

### 4. **Idempotency Protection**
- Implement unique idempotency keys for saga steps
- Check step execution history before re-execution
- Prevent duplicate debits/credits on retries

### 5. **Global Exception Handling**
- Add `@ControllerAdvice` for centralized error handling
- Return consistent error responses across endpoints
- Log and track saga failures systematically

### 6. **Additional Enhancements**
- Add saga timeout mechanisms
- Implement saga orchestration monitoring/observability
- Add compensation failure alerts
- Consider event sourcing for audit trails
- Add unit tests for saga compensation flows