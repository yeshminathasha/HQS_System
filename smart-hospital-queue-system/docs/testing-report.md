# Testing Report — Smart Hospital Queue System

## Summary

| Suite | Tests | Result |
|---|---|---|
| Backend unit + slice tests (Spring Boot 4.1.0) | 37 | ✅ All passing |
| Frontend component + hook tests (Vitest + Testing Library) | 15 | ✅ All passing |
| Frontend build | — | ✅ Production build succeeds (code-split routes) |
| Backend compile | — | ✅ `mvn compile` clean |
| Console app compile | — | ✅ `javac` clean + menu flow smoke-tested |

## Backend Tests

### Service layer — `PatientServiceImplTest` (15 tests)

| Test | Verifies |
|---|---|
| `registerGeneratesSequentialPatientId` | P-ID assignment from atomic sequence |
| `registerEmergencyWithoutValidPriorityThrows` | Priority must be 1–3 for emergencies |
| `registerNonEmergencyForcesZeroPriority` | Non-emergencies always get priority 0 |
| `registerWithUnknownDoctorThrows` | Registration rejects doctors not in the `doctors` collection |
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

### Service layer — `AppointmentServiceTest` (16 tests)

| Test | Verifies |
|---|---|
| `createAppointmentSucceedsAndUsesDoctorDepartment` | Booking succeeds; department taken from the doctor record |
| `createRejectsDoubleBookedDoctorSlot` | Same doctor/date/time → 409 |
| `createRejectsDoubleBookedPatientSlot` | Same patient/date/time → 409 |
| `createRejectsPastDate` | Past dates → 400 |
| `createRejectsUnknownDoctor` | Unknown doctor → 404 |
| `createRejectsDayOff` | Booking on a day the doctor does not work → 400 |
| `createRejectsOutsideWorkingHours` | Booking outside the doctor's working window → 400 |
| `cancelledSlotCanBeRebooked` | Cancelled/completed slots no longer block new bookings |
| `updateAppointmentReschedulesScheduled` | PUT reschedule updates date/time |
| `updateAppointmentIgnoresSelfInConflictCheck` | Reschedule to own current slot is not a conflict |
| `updateAppointmentRejectsCompleted` | Only SCHEDULED appointments can be rescheduled |
| `updateAppointmentNotFoundThrows` | Unknown appointment → 404 |
| `saveConflictMapsToDomainException` | Unique-index race (DuplicateKey) → 409, not 500 |
| `updateStatusOnlyAllowsCompleteOrCancelOnScheduled` | Status state machine for appointments |
| `getAppointmentsAppliesPagination` | Page envelope with skip/limit applied |
| `getAppointmentsFiltersByPatient` | `patientId` query param filters appointments |

### Controller slice tests — `PatientControllerTest` (6 tests)

| Test | Verifies |
|---|---|
| `registerValidPatientReturns201` | POST → 201 with P-ID |
| `registerInvalidPatientReturns400` | Validation errors → 400 with all field messages |
| `getMissingPatientReturns404Json` | Unknown ID → 404 with JSON error body |
| `updateStatusReturns200` | PATCH status → 200 |
| `updateStatusWithInvalidEnumReturns400` | Malformed status → 400 |
| `deletePatientReturns204` | DELETE → 204 |

## Frontend Tests (Vitest)

| File | Tests | Verifies |
|---|---|---|
| `Pagination.test.jsx` | 4 | Hides when single page; page info + record count; prev/next disable logic; page-change callback |
| `ToastProvider.test.jsx` | 4 | Success toast renders; auto-dismiss after timeout; confirm promise resolves true/false on Confirm/Cancel |
| `ErrorBoundary.test.jsx` | 2 | Renders children normally; fallback UI on child throw |
| `usePolling.test.js` | 5 | Immediate load; error surfacing; interval polling; pause on hidden + resume on visible; stale responses ignored after unmount |

## Manual Verification Checklist

- [x] Frontend `npm run build` passes; routes are code-split
- [x] Backend `mvn test` — 37/37 green
- [x] Console app compiles and the Call-Next → history flow runs end-to-end
- [x] Cancellation is a status transition; active patients cannot be deleted
- [x] Queue ordering by emergency → priority → registration time (unit-tested)
- [x] Duplicate appointment slots (same doctor/date/time, SCHEDULED only) rejected at service + index level
- [x] Rebooking a cancelled slot succeeds (unit-tested)
- [x] Doctor schedule (work days/hours) enforced on booking (unit-tested)
- [ ] End-to-end run against a live MongoDB instance
- [ ] Browser walkthrough of all 7 pages

## How to Run

```bash
cd backend
mvn test

cd frontend
npm test
```
