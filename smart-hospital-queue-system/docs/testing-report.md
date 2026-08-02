# Testing Report — Smart Hospital Queue System

## Summary

| Suite | Tests | Result |
|---|---|---|
| Backend unit + slice tests | 20 | ✅ All passing |
| Frontend build | — | ✅ Production build succeeds |
| Backend compile | — | ✅ `mvn compile` clean |
| Console app compile | — | ✅ `javac` clean |

## Backend Tests

### Service layer — `PatientServiceImplTest` (14 tests)

| Test | Verifies |
|---|---|
| `registerGeneratesSequentialPatientId` | P-ID assignment from atomic sequence |
| `registerEmergencyWithoutValidPriorityThrows` | Priority must be 1–3 for emergencies |
| `registerNonEmergencyForcesZeroPriority` | Non-emergencies always get priority 0 |
| `getActiveQueueUsesActiveStatusesAndPrioritySort` | Queue query filters active statuses and sorts emergency/priority/registeredAt |
| `updateStatusWaitingToConsultationSucceeds` | Valid transition WAITING → IN_CONSULTATION |
| `completeComputesWaitMinutes` | Completion records wait time and timestamp |
| `invalidTransitionThrows` | IN_CONSULTATION → WAITING rejected |
| `completedIsTerminal` | COMPLETED cannot be changed |
| `cancelWorks` | WAITING → CANCELLED allowed |
| `getPatientByIdNotFoundThrows` | Missing patient raises domain exception → 404 |
| `deleteActivePatientRejected` | Active patients cannot be hard-deleted |
| `deleteCancelledPatientSucceeds` | Terminal-state records can be purged |
| `waitingTimeUsesPositionInPriorityOrder` | Estimate = position × avg minutes |
| `waitingTimeForPatientNotInQueueThrows` | Non-queued patient → not found |

### Controller slice tests — `PatientControllerTest` (6 tests)

| Test | Verifies |
|---|---|
| `registerValidPatientReturns201` | POST → 201 with P-ID |
| `registerInvalidPatientReturns400` | Validation errors → 400 with field message |
| `getMissingPatientReturns404Json` | Unknown ID → 404 with JSON error body |
| `updateStatusReturns200` | PATCH status → 200 |
| `updateStatusWithInvalidEnumReturns400` | Malformed status → 400 |
| `deletePatientReturns204` | DELETE → 204 |

## Manual Verification Checklist

- [x] Frontend `npm run build` passes with no CSS warnings
- [x] Backend `mvn test` — 20/20 green
- [x] Cancellation is a status transition; active patients cannot be deleted
- [x] Queue ordering by emergency → priority → registration time (unit-tested)
- [x] Duplicate appointment slots (same doctor/date/time) rejected
- [ ] End-to-end run against a live MongoDB instance
- [ ] Browser walkthrough of all 7 pages

## How to Run

```bash
cd backend
mvn test
```
