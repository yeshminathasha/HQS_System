# Smart Hospital Queue System

A full-stack hospital queue management system: a **React (Vite)** frontend and a **Spring Boot (Java 17)** REST API backed by **MongoDB**. Patients register, join a priority‑based waiting queue, book appointments, and the system reports estimated wait times and daily statistics.

> **PDSA focus — data structures applied:**
> The backend implements its own **singly linked list** (`LinkedList` + `Node`) and **doubly linked list** (`DoublyLinkedList` + `DoublyLinkedNode`). The singly linked list models the **patient waiting queue** (FIFO, priority order), and the doubly linked list models the **patient history list** (newest‑first with forward + backward traversal). See [Data structures used](#data-structures-used).

---

## Repository root

```
smart-hospital-queue-system/
├── backend/                      Spring Boot 3.2.4 API (Maven)
│   ├── pom.xml                   dependencies: web, mongodb, validation, MapStruct
│   ├── .env.example              sample environment variables
│   └── src/
│       ├── main/
│       │   ├── java/com/smarthospital/
│       │   │   ├── SmartHospitalApplication.java    entry point
│       │   │   ├── config/        WebConfig (CORS), DataSeeder
│       │   │   ├── controller/    REST endpoints
│       │   │   ├── datastructure/ LinkedList + Node, DoublyLinkedList + DoublyLinkedNode ⬅ linked lists
│       │   │   ├── dto/           request/response models
│       │   │   ├── entity/        MongoDB documents (Patient, Doctor, Appointment, Sequence)
│       │   │   ├── exception/     error models + global handler
│       │   │   ├── mapper/        MapStruct PatientMapper
│       │   │   ├── repository/    Spring Data MongoDB repositories
│       │   │   └── service/       business logic (queue, history, reports, appointments)
│       │   └── resources/
│       │       ├── application.yml                    config + MONGODB_URI
│       │       └── META-INF/spring-configuration-metadata.json
│       └── test/java/            unit + MVC tests (JUnit 5 / Mockito)
└── frontend/                    React 18 + Vite + Tailwind
    ├── package.json
    ├── vite.config.js           dev server :3000, proxies /api → :8080
    └── src/
        ├── App.jsx, main.jsx    routing + layout
        ├── pages/               Dashboard, PatientQueue, Register, Emergency, Appointments, History, Reports
        ├── components/          QueueDisplay, PatientForm, EmergencyPanel, DoctorAvailability, ...
        ├── hooks/usePolling.js  polls the queue API every few seconds
        └── services/api.js      configured axios client
```

---

## Tech stack

| Layer      | Technology                                            |
|------------|-------------------------------------------------------|
| Frontend   | React 18, Vite 5, Tailwind CSS 4, React Router, axios |
| Backend    | Java 17, Spring Boot 3.2.4, Spring Data MongoDB, MapStruct, Bean Validation |
| Database   | MongoDB (works with Atlas or a local `mongod`)        |
| Build      | Maven, npm                                             |

---

## Backend architecture

Layered (Controller → Service → Repository/Database). Requests arrive at a `@RestController`, are validated, delegated to a `@Service`, which persists via a Spring Data `MongoRepository` (or `MongoTemplate` for aggregations). MapStruct generates the `PatientMapper` implementing `toEntity`/`toResponse`. `@RestControllerAdvice` (GlobalExceptionHandler) converts exceptions into a uniform `ErrorResponse` JSON.

```
HTTP ─► Controller ─► Service ─► Repository / MongoTemplate ─► MongoDB
   ▲      │  (validation)   │
   └──────└── ErrorResponse └── DTOs (via MapStruct mapper)
```

### Data model (MongoDB database `smarthospital`)

| Collection    | Entity        | Notes                                            |
|---------------|---------------|--------------------------------------------------|
| `patients`    | `Patient`     | status lifecycle: `WAITING → IN_CONSULTATION → COMPLETED`, `CANCELLED` |
| `doctors`     | `Doctor`      | work days/hours; 4 seeded on startup via `DataSeeder` |
| `appointments`| `Appointment` | doctor + patient slot booking, `SCHEDULED/COMPLETED/CANCELLED` |
| `sequences`   | `Sequence`    | auto-increment counters used to generate patient ids (`P001`, `P002`, …) |

---

## Data structures used

### 1. Singly linked list — the active waiting queue

`com.smarthospital.datastructure.LinkedList<T>` is a generic singly linked list with a `head` and `tail` pointer. Enqueueing happens with `addLast(...)` (append at tail, O(1)), dequeueing with `removeFirst()` (O(1)).

Application: `PatientServiceImpl.getSortedActiveQueue()` (backend).

- Patients in `WAITING` / `IN_CONSULTATION` are pulled from Mongo and ordered by **emergency (desc) → priorityLevel (asc) → registeredAt (asc)**.
- Each patient is enqueued with `queue.addLast(patient)`, forming a FIFO queue that already respects priority.
- `getActiveQueue()` serves it as JSON via `queue.toList()`.
- `getEstimatedWaitingTime()` walks the list by index (`get(i)`) to compute the patient's **position**, **patients ahead**, and **estimated minutes** (`i × avg-service-minutes`).

### 2. Doubly linked list — the patient history list

`com.smarthospital.datastructure.DoublyLinkedList<T>` is a generic doubly linked list (`head`/`tail` + `prev`/`next` links) supporting efficient additions at both ends, `removeFirst()`/`removeLast()`, and traversal in **both** directions (`head().next()...` / `tail().prev()...`), plus `toList()` and `toReversedList()`.

Application: `PatientServiceImpl.getHistory()` — **history of completed/cancelled patients, newest first**.

- Records are fetched newest‑first (`registeredAt` desc) and appended with `addLast(...)`, so the **head is the newest** record and the **tail the oldest**.
- Forward traversal (`head` → `tail`) yields newest→oldest for display; the `prev` link allows a quick backward pass (oldest→newest) without re‑querying or re‑sorting.

### Summary

| Place                                        | Data structure used                                       | Location |
| -------------------------------------------- | -------------------------------------------------------- | -------- |
| Active waiting queue (FIFO in priority order) | Custom **singly linked list** (`LinkedList`/`Node`)        | `backend/.../datastructure/LinkedList.java`, `backend/.../service/PatientServiceImpl.java` |
| Patient history list (newest‑first, two‑way walk) | Custom **doubly linked list** (`DoublyLinkedList`/`DoublyLinkedNode`) | `backend/.../datastructure/DoublyLinkedList.java`, `backend/.../service/PatientServiceImpl.java` |
| Ordered aggregation results (order kept)      | `java.util.LinkedHashMap` (internally a doubly linked list) | `backend/.../service/DoctorService.java`, `backend/.../service/ReportService.java` |
| List data generally                          | `java.util.List` / `ArrayList`                            | services, controllers, mappers |
| Auto-increment patient IDs                   | Mongo `findAndModify` counter (`SequenceService`)         | `backend/.../service/SequenceService.java` |

---

## REST API

| Method | Path                                  | Description                          |
| ------ | ------------------------------------- | ------------------------------------ |
| POST   | `/api/patients`                        | Register a patient                   |
| GET    | `/api/patients/queue`                  | Active priority queue (`search`, `department`, `doctor`, `emergency`, `status`) |
| GET    | `/api/patients/history`                | Completed/cancelled patients         |
| GET    | `/api/patients/{id}`                   | Patient by ID                        |
| GET    | `/api/patients/{id}/wait-time`         | Position + estimated wait            |
| PUT    | `/api/patients/{id}`                   | Update patient                       |
| PATCH  | `/api/patients/{id}/status`            | Move status to `IN_CONSULTATION`/`COMPLETED`/`CANCELLED` |
| DELETE | `/api/patients/{id}`                   | Delete (only non-active)             |
| GET    | `/api/doctors`                         | Doctors with active queue workload   |
| GET    | `/api/doctors/recommend`               | Least-loaded doctor                  |
| POST   | `/api/appointments`                    | Book appointment                     |
| GET    | `/api/appointments`                    | Filter by `doctor`, `date`, `upcoming` |
| PATCH  | `/api/appointments/{id}/status`        | `COMPLETED`/`CANCELLED`               |
| GET    | `/api/reports/daily`               | Daily report (counts + avg wait)     |

---

## Frontend

Vite dev server on **:3000**, proxying `/api` to `http://localhost:8080` (defined in `vite.config.js`). Pages: **Dashboard**, **Patient Queue** (live polling), **Register**, **Emergency**, **Appointments**, **History**, **Reports**. CORS is configured in `backend/.../config/WebConfig.java` (default `http://localhost:3000`).

---

## Running the project

### 1. Database (local MongoDB)
The easiest is a portable `mongod` (e.g. extracted under `Temp\opencode\mongo\...\bin\mongod.exe`):

```powershell
mongod --dbpath <your-data-dir> --port 27017 --bind_ip 127.0.0.1
```

Verify with MongoDB Compass: connect `mongodb://localhost:27017/smarthospital` → collections `patients`, `doctors`, `appointments`, `sequences`.

Alternatively use MongoDB Atlas and set the full `mongodb+srv://...` connection string in the `MONGODB_URI` environment variable.

### 2. Backend (`:8080`)

```powershell
cd backend
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/smarthospital"
mvn spring-boot:run
```

Configuration lives in `backend/src/main/resources/application.yml`; secrets stay out of the repo via the `MONGODB_URI` / `CORS_ALLOWED_ORIGINS` / `PORT` environment overrides. The `DataSeeder` inserts 4 demo doctors on first run.

### 3. Frontend (`:3000`)

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:3000. `mvn clean install` runs the backend unit tests (JUnit 5 / Mockito), including the `LinkedList` and `DoublyLinkedList` data-structure tests.