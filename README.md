# Case Service

The **case-service** consumes lab events from Kafka and manages the lifecycle of
patients, cases, and associated lab results.

This service is the **core domain service** responsible for:
- Patient resolution
- Case creation and lifecycle management
- Lab-to-case attachment
- Ensuring data consistency and idempotent processing

---

## Responsibilities

- Consume `LabEvent` messages from Kafka
- Match or create patients
- Resolve or create active cases
- Attach lab results to the correct case
- Maintain case lifecycle (`OPEN`, `CLOSED`, etc.)
- Provide traceable, auditable processing with correlation IDs

---

## High-Level Flow

1. **Receive LabEvent from Kafka**
2. **Patient Resolution**
    - Match by `firstName + lastName + dob`
    - Create patient if not found
3. **Case Resolution**
    - If an active case exists → reuse it
    - Otherwise → create a new case
4. **Persist Lab Result**
    - Attach lab to patient + case
5. **Emit structured logs with correlation ID**

---

## Case Resolution Logic

### Default Flow (Authoritative)

- Patient identity is the source of truth
- Only **one active case per patient**
- New labs attach to the active case
- Closed cases are never reused

---

## CaseId Fast-Path (Optional Hint)

If `caseId` is present in the event:

1. Validate UUID
2. Load case
3. Ensure case is **ACTIVE**
4. Validate patient identity
5. Attach lab directly

If any check fails → fall back to patient matching logic.

### Purpose

- Replay jobs
- Internal re-publishing
- Faster attachment
- Fewer false matches

> `caseId` is treated as a **hint**, never as authority.

---

## Transaction Boundaries

- Each lab ingestion is processed in a **single transaction**
- Ensures atomicity across:
    - Patient
    - Case
    - Lab result
- Prevents partial writes

---

## Idempotency & Safety

Kafka provides *at-least-once* delivery.

The service is designed to be replay-safe and resilient to duplicate events.

- Duplicate lab events may be received
- Case resolution logic prevents multiple active cases
- Lab persistence is safe for reprocessing

Future enhancement:
- Explicit idempotency keys per lab event

---

## Auditing & Timestamps

All entities inherit from a shared `AuditableEntity`:

- `created_at`
- `updated_at`

Handled via JPA lifecycle hooks:
- `@PrePersist`
- `@PreUpdate`

Guarantees:
- No manual timestamp handling
- Consistent auditing across entities

---

## Correlation IDs & Observability

- Correlation ID propagated from Kafka headers
- Stored in MDC
- Included in all logs

Example:
```text
[demo-002] LabIngested patientId=... caseId=... labId=...

```
---

## Reliability & Failure Handling (PR-2)

To ensure production-grade robustness, the case-service implements **controlled retry**, **DLQ publishing**, and **failure persistence**.

### Retry Strategy

- Uses Spring Kafka `DefaultErrorHandler`
- Retries failed records **2 times**
- Fixed backoff: **1 second**
- Total attempts: **3** (1 initial + 2 retries)

Each retry attempt is logged with:
- correlationId
- topic
- partition
- offset
- attempt number
- error type

This helps transient failures (e.g., temporary DB/network issues) recover automatically.

### Non-Retryable Exceptions (Poison Pills)

The following exceptions are **not retried** (sent directly to final recovery):

- `JsonProcessingException`
- `IllegalArgumentException`

This prevents infinite retry loops for malformed payloads.

### Dead Letter Queue (DLQ)

When retries are exhausted (or for non-retryable errors), the record is published to:

```text
<original-topic>.DLQ
```

Example:

```text
lab.events → lab.events.DLQ
```

Partition is preserved to maintain ordering guarantees per partition.

### Failure Persistence: case_ingest_error

All failed records are stored in the table:

```text
case_ingest_error
```

Stored metadata includes:
- created_at
- topic
- partition
- kafka_offset
- message_key
- correlation_id
- error_type
- error_message
- payload
- status (NEW, FIXED, REPLAYED, IGNORED)

### Why This Matters

Logs are ephemeral. The database guarantees:

- Auditability
- Traceability
- Replay capability
- Production support workflows

Failures are persisted using:

```text
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

This ensures:
- Even if the main transaction rolls back
- The failure record is committed

### Failure Processing Flow

1. Consumer receives record 
2. Business logic throws exception 
3. Kafka retries (based on backoff policy)
4. After retries are exhausted:
    - Failure is stored in `case_ingest_error`
    - Record is published to DLQ
    - Offset is committed (record will not reprocess automatically)


## Current System State

The case-service now supports:
- Domain workflow processing 
- Safe case resolution logic 
- CaseId fast-path optimization 
- Transactional guarantees 
- Auditable timestamps 
- Correlation ID tracing 
- Controlled retries 
- DLQ publishing 
- Persistent failure storage