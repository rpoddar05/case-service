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

- Kafka provides *at-least-once* delivery
- Duplicate lab events may be received
- Processing is designed to be safe for replays

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