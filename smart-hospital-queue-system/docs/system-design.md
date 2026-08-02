# System Design — Smart Hospital Queue System

## Overview

Three-tier architecture: React SPA (frontend) → Spring Boot REST API (backend) → MongoDB (database).
A standalone Java console application demonstrates the same domain logic with hand-written
Doubly Linked List data structures.

## Architecture Diagram

```
┌─────────────────┐      HTTP/JSON       ┌──────────────────┐      Mongo Driver       ┌──────────────┐
│  React + Vite   │ ───────────────────► │  Spring Boot 3   │ ─────────────────────► │   MongoDB    │
│  (port 3000)    │  ◄─────────────────── │  (port 8080)    │  ◄───────────────────── │  smarthospital│
└─────────────────┘  /api proxied + CORS └──────────────────┘                         └──────────────┘
```

## Backend Design

### Layered structure

- **Controller layer** — HTTP concerns only; validation via `@Valid` on DTOs
- **Service layer** — business rules (priority ordering, status state machine, double-booking checks)
- **Repository layer** — Spring Data MongoDB repositories + `MongoTemplate` for aggregations
- **Entity layer** — MongoDB documents with explicit indexes
- **Mapper layer** — MapStruct entity ↔ DTO mapping

### Queue algorithm

The active queue is derived (not stored) by querying patients with status
`WAITING` or `IN_CONSULTATION`, sorted by:

1. `emergency` descending (emergencies first)
2. `priorityLevel` ascending (severity 1 before 2 before 3)
3. `registeredAt` ascending (FIFO within the same priority)

This keeps ordering consistent and avoids maintaining a separate queue collection.

### Status state machine

```
WAITING ──────────► IN_CONSULTATION ──────────► COMPLETED
   │                      │
   └──────────┬───────────┘
              ▼
          CANCELLED        (terminal: COMPLETED / CANCELLED cannot change)
```

Cancellation is a state transition — records are never deleted while active, preserving
history and enabling daily reports. Hard deletion is allowed only for terminal states.

### Waiting time

`position_in_sorted_queue × avg-service-minutes` (configurable, default 15). Actual wait
duration is recorded when a patient is completed (`waitMinutes`) and feeds the daily report.

### ID generation

P-IDs (`P001`, `P002`, ...) are generated atomically from a `sequences` collection using
`findOneAndUpdate` with `$inc` + upsert, eliminating the previous race condition and
post-deletion collision bug. `patientId` has a unique index as a second line of defense.

### Daily report

MongoDB aggregation pipelines group registrations by status, department, and doctor for a
given day, plus the average measured wait time of completed patients.

## Frontend Design

- **State & refresh** — `usePolling` hook polls the API every 10s (queue) / 30s (stats),
  exposing `loading`, `error`, `lastUpdated`, and manual `refresh`
- **Pages** — Dashboard, Patient Queue, Emergency, Appointments, History, Register, Reports
- **Reusable components** — `PatientForm` (registration), `QueueDisplay` (queue table),
  `EmergencyPanel`, `DoctorAvailability`, `ReportsView`, plus UI primitives (`Button`, `Card`, `Badge`)
- **API layer** — axios instance with centralized error normalization; base URL from
  `VITE_API_URL`, dev proxy `/api → :8080`

## Data Flow

1. Reception registers a patient → `POST /api/patients` → document stored, P-ID assigned
2. Emergency patients get priority in the sorted queue
3. Doctor calls next → `PATCH /status` WAITING → IN_CONSULTATION
4. Doctor completes → IN_CONSULTATION → COMPLETED, wait time recorded
5. Daily report aggregates stored documents; history page reads completed/cancelled records

## Console Application

Mirrors the same domain with custom Doubly Linked Lists:

- `PatientQueueDLL` — priority-aware insert (emergency sorted by level), delete, search
- `AppointmentHistoryDLL` — append-only history with forward/reverse traversal

History stores immutable snapshots (copy constructor) so later queue updates do not
retroactively mutate historical records.
