# Smart Hospital Queue System

A full-stack patient queue and appointment management system for hospitals.

- **Frontend:** React 18 + Vite 5 + Tailwind CSS 4
- **Backend:** Java 17 + Spring Boot 4 + MongoDB
- **Console demo:** Java console app demonstrating custom Doubly Linked List data structures
- **Docs:** See `docs/` for system design and testing reports

## Features

| Feature | Web App | Console App |
|---|---|---|
| Patient registration (auto-generated P-IDs) | ✅ | ✅ |
| Priority queue — emergencies first, by severity | ✅ | ✅ |
| Queue management (Call Next / Complete / Cancel) | ✅ | ✅ |
| Appointment booking, rescheduling, completion, cancellation | ✅ | ❌ |
| Estimated waiting time | ✅ | ✅ |
| Doctor recommendation (shortest queue) | ✅ | ✅ |
| Doctor availability, workload & real schedules | ✅ | ❌ |
| Patient history (completed + cancelled), paginated, per-patient view | ✅ | ✅ |
| Daily report (registrations, completions, cancellations, avg wait) | ✅ | ❌ |
| Search & filtering | ✅ | ✅ |
| Live refresh (10s polling) | ✅ | ❌ |

## Architecture

```
frontend/  React SPA (Vite, Tailwind, axios)
backend/   Spring Boot REST API (MongoDB)
java-console/  Standalone console app (DLL-based queue)
docs/      Design & testing documentation
```

### Backend structure

```
com.smarthospital
├── config/      CORS config, demo-data seeder, index setup
├── controller/  Patient, Appointment, Doctor, Report REST controllers
├── dto/         Request/response DTOs + error response + page envelope
├── entity/      Patient, Appointment, Doctor, Sequence, status enums
├── exception/   Domain exceptions + global exception handler
├── mapper/      MapStruct entity <-> DTO mapper
├── repository/  Spring Data MongoDB repositories
└── service/     Queue, appointment, doctor, report, sequence services
```

The queue is sorted server-side by `emergency (desc) → priorityLevel (asc) → registeredAt (asc)`, so emergencies are always served first. Cancelling an appointment is a status transition (`CANCELLED`), never a delete — history and daily reports remain intact. Cancelled/completed appointment slots can be rebooked; a partial unique index prevents double-booking of the same doctor/slot at the database level.

## Getting Started

### Prerequisites

- Java 17+
- MongoDB running locally (default `mongodb://localhost:27017/smarthospital`)
- Node.js 18+

### 1. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API runs on `http://localhost:8080`. Demo doctors are seeded automatically on first start.

### 2. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI runs on `http://localhost:3000` and proxies `/api` to the backend (configurable via `VITE_API_URL`).

### 3. (Optional) Console demo

```bash
cd java-console
javac -d bin (Get-ChildItem -Recurse src -Filter *.java | % { $_.FullName })
java -cp bin main.HospitalManagementSystem
```

## Configuration

| Variable | Default | Used by |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/smarthospital` | Backend |
| `PORT` | `8080` | Backend |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Backend |
| `VITE_API_URL` | `/api` (proxied in dev) | Frontend |
| `queue.avg-service-minutes` | `15` | Backend wait-time estimate |

Set backend variables as real environment variables (Spring Boot does not read `.env` files).

## API Overview

| Method | Route | Description |
|---|---|---|
| POST | `/api/patients` | Register patient (returns generated P-ID) |
| GET | `/api/patients/queue` | Active queue, priority-ordered; filters: `search`, `department`, `doctor`, `emergency` |
| GET | `/api/patients/history?page=&size=` | Completed/cancelled patients; filter: `search`; returns page envelope |
| GET | `/api/patients/{id}` | Get patient by P-ID |
| GET | `/api/patients/{id}/history?page=&size=` | History of one patient |
| GET | `/api/patients/{id}/wait-time` | Estimated waiting time |
| PUT | `/api/patients/{id}` | Update patient details |
| PATCH | `/api/patients/{id}/status` | Transition: WAITING → IN_CONSULTATION → COMPLETED; → CANCELLED |
| POST | `/api/appointments` | Book an appointment (validates doctor, schedule, past dates; double-booking rejected) |
| GET | `/api/appointments?upcoming=true&patientId=&page=&size=` | List appointments (page envelope) |
| PUT | `/api/appointments/{id}` | Reschedule a scheduled appointment |
| PATCH | `/api/appointments/{id}/status` | Complete/cancel appointment |
| GET | `/api/doctors` | Doctors with live workload and schedules |
| GET | `/api/doctors/recommend` | Recommended doctor (shortest queue) |
| GET | `/api/reports/daily?date=YYYY-MM-DD` | Daily report with aggregations |

Paginated list endpoints return `{ content, page, size, totalElements, totalPages }` (default `size=25`, max `100`).

Errors are returned as JSON: `{ timestamp, status, error, message, path }` (404 for missing resources, 400 for validation, 409 for invalid state transitions).

## Testing

```bash
cd backend
mvn test
```

See `docs/testing-report.md` for the current test summary.

## Data Model

- **patients** — registration, queue position, status lifecycle, wait-time measurement (`patientId` unique indexed; compound index `status → emergency → priorityLevel → registeredAt` for the live queue)
- **appointments** — bookings with doctor/date/time (double-booking prevented by validation + partial unique index on scheduled slots)
- **doctors** — departments and working schedules (booking validates the doctor works on that day/time)
- **sequences** — atomic P-ID generation counter
