# Smart Hospital Patient Queue and Appointment Management System
## Complete Step-by-Step Development Guide (Doubly Linked List Project)

Based on your proposal: HND Software Engineering, PDSA Course Work, using **Doubly Linked List** as the core data structure.

---

## STEP 0 — Understand What You're Actually Building

Before touching code, be clear on the architecture:

- **The Doubly Linked List (DLL) is your core logic** — it lives on the **backend**, and it manages the *live in-memory patient queue* (insert, delete, traverse forward/backward, priority insertion for emergencies).
- **The Database** is for **persistence** — storing patient records, appointment history, and reports so data survives a server restart. The DLL is rebuilt from the DB (or kept in memory while the server runs) — the DB is *not* the DLL itself.
- **The Frontend** is the interface hospital staff/admins use to register patients, view the live queue, and generate reports.

This separation (DLL logic layer → API layer → DB layer → UI layer) is exactly what your lecturer will want to see explained clearly in your viva.

---

## STEP 1 — Choose the Tech Stack

Recommended stack for your project (simple to justify, easy to demo, good GitHub story):

| Layer | Technology | Why |
|---|---|---|
| Frontend | **React.js (Vite)** + Tailwind CSS | Fast setup, component-based UI, easy to show live queue updates |
| Backend | **Node.js + Express.js** | JavaScript on both ends = simpler for a 4-person team, DLL is easy to implement in JS classes |
| Database | **MongoDB** (with Mongoose) | Flexible schema — good for patient records, appointments, history logs. Easy to set up locally or free-tier on MongoDB Atlas |
| Realtime (optional novel feature) | **Socket.io** | Push live queue updates to the frontend instantly (supports your "Real-Time Waiting Time Prediction" feature) |
| Version Control | **Git + GitHub** | Team collaboration, viva evidence of individual contribution via commit history |

> Alternative: If your module requires a strictly relational DB, swap MongoDB → **MySQL + Sequelize**. Everything else below stays the same, just note the DB layer changes.

---

## STEP 2 — Set Up GitHub FIRST (before writing code)

Since contribution is assessed individually via commit history, do this before any coding:

1. One team member creates a **GitHub organization or repo**:
   ```
   Repo name: smart-hospital-queue-system
   Visibility: Private (add lecturer as collaborator if required)
   ```
2. Initialize with a `.gitignore` (Node template), `README.md`, and MIT/Apache license (optional).
3. Add all 4 team members as **collaborators**.
4. Create a **branching strategy** so individual contribution is traceable:
   ```
   main          → stable, working code only
   dev           → integration branch
   feature/dll-queue-logic       → person A
   feature/appointment-api       → person B
   feature/frontend-ui           → person C
   feature/reports-history       → person D
   ```
5. Set branch protection on `main` (require pull requests) — this creates a clean PR history your lecturer can review per student.
6. Each member clones the repo locally:
   ```bash
   git clone https://github.com/<org>/smart-hospital-queue-system.git
   cd smart-hospital-queue-system
   ```
7. Agree on a commit message convention, e.g.:
   ```
   feat: add emergency priority insertion to DLL
   fix: correct backward traversal bug
   docs: update README with setup steps
   ```

This alone gives you strong viva evidence — each member's git log shows exactly what they built.

---

## STEP 3 — Create the Project Folder Structure

```
smart-hospital-queue-system/
├── backend/
│   ├── src/
│   │   ├── dataStructures/
│   │   │   └── DoublyLinkedList.js       ← core DLL implementation
│   │   ├── models/
│   │   │   ├── Patient.js
│   │   │   ├── Appointment.js
│   │   │   └── Doctor.js
│   │   ├── controllers/
│   │   │   ├── patientController.js
│   │   │   ├── appointmentController.js
│   │   │   └── queueController.js
│   │   ├── routes/
│   │   │   ├── patientRoutes.js
│   │   │   ├── appointmentRoutes.js
│   │   │   └── queueRoutes.js
│   │   ├── services/
│   │   │   └── queueService.js           ← wraps DLL, business logic
│   │   ├── config/
│   │   │   └── db.js
│   │   ├── sockets/
│   │   │   └── queueSocket.js            ← optional realtime push
│   │   └── app.js
│   ├── tests/
│   │   └── DoublyLinkedList.test.js      ← unit tests for DLL
│   ├── package.json
│   └── .env.example
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── PatientForm.jsx
│   │   │   ├── QueueDisplay.jsx
│   │   │   ├── EmergencyPanel.jsx
│   │   │   ├── DoctorAvailability.jsx
│   │   │   └── ReportsView.jsx
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Register.jsx
│   │   │   └── History.jsx
│   │   ├── services/
│   │   │   └── api.js                    ← axios calls to backend
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── literature-review.md
│   ├── system-design.md
│   └── testing-report.md
├── .gitignore
└── README.md
```

---

## STEP 4 — Install Dependencies (do this before writing feature code)

### Backend
```bash
mkdir backend && cd backend
npm init -y

# Core
npm install express mongoose dotenv cors

# Realtime (for live queue + waiting time updates)
npm install socket.io

# Dev tools
npm install --save-dev nodemon jest supertest
```

Add to `backend/package.json` scripts:
```json
"scripts": {
  "start": "node src/app.js",
  "dev": "nodemon src/app.js",
  "test": "jest"
}
```

### Frontend
```bash
cd .. 
npm create vite@latest frontend -- --template react
cd frontend
npm install

# HTTP + realtime + styling
npm install axios socket.io-client
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

### Database
- Install **MongoDB Community Server** locally, OR create a free cluster on **MongoDB Atlas** (recommended — no local setup, works for all 4 team members, easy demo).
- Create `backend/.env`:
  ```
  MONGO_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/hospitalQueueDB
  PORT=5000
  ```

---

## STEP 5 — Design the Database Schema

Even though the DLL manages the *live queue in memory*, MongoDB stores the persistent records:

**Patient**
```js
{
  patientId: String,      // unique
  name: String,
  contactNumber: String,
  registeredAt: Date
}
```

**Doctor**
```js
{
  doctorId: String,
  name: String,
  department: String
}
```

**Appointment**
```js
{
  appointmentId: String,
  patientId: String,
  doctorId: String,
  department: String,
  dateTime: Date,
  isEmergency: Boolean,
  priorityLevel: Number,   // 1 = highest
  status: String,          // "waiting" | "in-progress" | "completed" | "cancelled"
  estimatedWaitTime: Number,
  createdAt: Date
}
```

**AppointmentHistory** (for reports)
```js
{
  patientId: String,
  appointmentId: String,
  outcome: String,
  completedAt: Date
}
```

---

## STEP 6 — Implement the Doubly Linked List (the core of your project)

This is what your viva will focus on most. Build it as a **standalone, well-tested class**, independent of Express — this proves you actually understand the data structure rather than just calling a library.

`backend/src/dataStructures/DoublyLinkedList.js` — implement:

- `Node` class: `data`, `prev`, `next`
- `DoublyLinkedList` class with:
  - `insertAtEnd(patientData)` — normal patient joins queue
  - `insertEmergency(patientData)` — **novel feature #1**: walks from head and inserts based on `priorityLevel`, ahead of lower-priority patients, without disturbing the rest of the queue
  - `deleteById(patientId)` — patient seen / appointment cancelled
  - `traverseForward()` / `traverseBackward()` — admin navigation, supports "view previous/next appointment"
  - `updateNode(patientId, newData)` — reschedule/update
  - `search(patientId)` — patient lookup
  - `calculateEstimatedWaitTime(patientId)` — **novel feature #2**: sums average consultation time × position in queue from head to that node
  - `getQueueSnapshot()` — returns array for API/frontend display

Write this with **plain JS, no shortcuts** (manual pointer manipulation) — that's the whole point of the assignment.

`backend/tests/DoublyLinkedList.test.js` — unit test every operation (insert, delete, emergency priority ordering, traversal, edge cases like empty list / single node / deleting head or tail).

**Doctor Availability Recommendation (novel feature #3)**: a separate service that scans each doctor's queue length (each doctor can have their own DLL instance) and returns the doctor with the shortest queue — implement in `services/queueService.js`.

---

## STEP 7 — Build the Backend API Layer

Wrap the DLL in Express routes:

| Method | Route | Purpose |
|---|---|---|
| POST | `/api/patients` | Register new patient |
| GET | `/api/patients/:id` | Get patient record |
| POST | `/api/appointments` | Create appointment (inserts into DLL) |
| PUT | `/api/appointments/:id` | Update/reschedule |
| DELETE | `/api/appointments/:id` | Cancel appointment |
| POST | `/api/appointments/emergency` | Insert emergency patient |
| GET | `/api/queue` | Get live queue snapshot |
| GET | `/api/queue/wait-time/:patientId` | Estimated wait time |
| GET | `/api/doctors/recommend` | Doctor with shortest queue |
| GET | `/api/reports/daily` | Daily appointment report |
| GET | `/api/history/:patientId` | Patient appointment history |

On server start, load pending appointments from MongoDB into the DLL so the queue isn't lost on restart; every DLL mutation also writes through to MongoDB for persistence.

---

## STEP 8 — Build the Frontend

Using React + Tailwind, build these screens matching your proposal's inputs/outputs:

1. **Register Patient** — form for Patient ID, Name, Contact, Doctor, Department, Date/Time, Emergency Yes/No, Priority Level.
2. **Live Queue Dashboard** — real-time list (via Socket.io) showing current queue order, emergency patients flagged/highlighted at top.
3. **Estimated Wait Time** — shown per patient card.
4. **Doctor Availability Panel** — recommended doctor with shortest queue.
5. **Reports Page** — daily appointment report, appointment history search by Patient ID.
6. **Appointment Management** — update/cancel/reschedule actions.

Connect via `frontend/src/services/api.js` using Axios, and a Socket.io client to receive live queue push updates.

---

## STEP 9 — Conduct the Literature Review (required deliverable)

Before finalizing your "novel features," research 3–4 existing systems, e.g.:
- Existing government hospital e-channelling systems (e.g. Sri Lanka's Channel-based booking platforms)
- Private hospital queue management software
- Generic appointment-booking SaaS tools

For each, document in `docs/literature-review.md`:
- What it does
- What data structures/approach it likely uses (linear list, simple FIFO queue, DB-only booking)
- Its limitations (e.g., no emergency prioritization, no live wait-time estimate, no doctor-load balancing)

Then explicitly map your 3 novel features to the gaps you found — this is exactly what your proposal promises and what examiners check first.

---

## STEP 10 — Testing & Validation

- **Unit tests**: `DoublyLinkedList.test.js` — every DLL operation, especially edge cases (empty queue, single patient, deleting head/tail, emergency insertion ordering).
- **API tests**: use `supertest` to hit each endpoint.
- **Manual test cases table** (put in `docs/testing-report.md`): Test ID, Input, Expected Output, Actual Output, Pass/Fail — for both normal and emergency queue scenarios.
- **Integration test**: register 5+ patients, insert 1 emergency mid-queue, confirm ordering, confirm wait-time recalculation, confirm doctor recommendation updates.

---

## STEP 11 — GitHub Workflow During Development

1. Each member works on their `feature/*` branch, commits regularly with meaningful messages tied to their part (DLL logic / API / frontend / reports).
2. Open a Pull Request into `dev` when a feature is ready; at least one other teammate reviews/approves.
3. Merge `dev` → `main` once integrated and tested.
4. Use **GitHub Issues** to track tasks (one issue per feature/bug) — assign to individual members, this becomes more viva evidence.
5. Tag a release (`v1.0`) once the final version is stable, and add the final `README.md` with setup instructions, screenshots, and architecture diagram.

---

## STEP 12 — Final README Checklist (top of repo)

Your `README.md` should include:
- Problem statement (from your proposal's Introduction)
- Architecture diagram (Frontend ↔ API ↔ DLL logic ↔ MongoDB)
- Data structure justification (same as your proposal's DLL justification section)
- Setup instructions (clone, `npm install` both folders, `.env` setup, `npm run dev` both)
- Novel features list
- Team members & individual contribution summary
- Screenshots of the running app

---

## Suggested Order to Actually Do This (summary)

1. GitHub repo + branch structure
2. Folder structure + install dependencies (backend & frontend)
3. MongoDB schema + connection
4. Doubly Linked List class + unit tests (do this in isolation first, no Express yet)
5. Wrap DLL in Express API + connect to MongoDB for persistence
6. Build React frontend screens, connect via Axios/Socket.io
7. Implement the 3 novel features end-to-end
8. Literature review write-up
9. Testing & validation docs
10. Final README + demo prep for viva
