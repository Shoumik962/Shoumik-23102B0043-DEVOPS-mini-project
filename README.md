# Food Waste Tracking Dashboard — DevOps Mini Project

[![DevOps CI Pipeline](https://github.com/Shoumik962/Shoumik-23102B0043-DEVOPS-mini-project/actions/workflows/ci.yml/badge.svg)](https://github.com/Shoumik962/Shoumik-23102B0043-DEVOPS-mini-project/actions)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-Pipeline-red.svg)](https://www.jenkins.io/)

A full-stack, containerized Food Waste Tracking Dashboard built for DevOps automation, continuous integration, and real-time food surplus reduction telemetry across campus dining halls, cafeterias, and commercial kitchens.

---

## 🏗️ Architecture Overview

```
                          ┌───────────────────────────┐
                          │   Client Browser / SPA    │
                          │   (React 18 + Tailwind)   │
                          └─────────────┬─────────────┘
                                        │  Port 5173 / 3000
                                        ▼
             ┌─────────────────────────────────────────────────────┐
             │       Reverse Proxy / Dev Proxy (/api/*)            │
             └──────────────────────────┬──────────────────────────┘
                                        │  Port 8081
                                        ▼
                 ┌──────────────────────────────────────────┐
                 │          Java REST API Backend           │
                 │      (Embedded High-Perf HTTP + Gson)     │
                 │                                          │
                 │  - GET    /api/health (DevOps Health)    │
                 │  - GET    /api/stats  (Telemetry)        │
                 │  - GET    /api/entries (Logs & Filter)   │
                 │  - POST   /api/entries (New Records)     │
                 │  - PUT    /api/entries/:id (Update/Audit)│
                 │  - DELETE /api/entries/:id (Removal)     │
                 └──────────────────────────────────────────┘
```

---

## 🚀 Quick Start (Run Locally)

### Option 1: Unified Start Script (Recommended)
You can start both backend (port `8081`) and frontend (port `5173`) with a single command:

```bash
./start.sh
# or using npm
npm start
```
- **Frontend Dashboard**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8081](http://localhost:8081)
- **Health Check**: [http://localhost:8081/api/health](http://localhost:8081/api/health)

Press `Ctrl+C` to gracefully terminate both services.

---

### Option 2: Docker Compose
Run the fully containerized multi-service stack with Nginx and Java runtime:

```bash
docker compose up --build
```
- **Frontend Container (Nginx)**: [http://localhost:3000](http://localhost:3000)
- **Backend Container**: [http://localhost:8081](http://localhost:8081)

To stop:
```bash
docker compose down
```

---

### Option 3: Manual Execution

#### 1. Start Backend:
```bash
cd backend
mvn clean package
java -jar target/tracker-backend.jar
```

#### 2. Start Frontend:
```bash
cd frontend
npm install
npm run dev
```

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | DevOps health check (uptime, memory, status) |
| `GET` | `/api/stats` | Aggregated waste statistics & category breakdown |
| `GET` | `/api/entries` | List waste entries (supports `?search=`, `?status=`, `?category=`) |
| `POST` | `/api/entries` | Create new waste entry record |
| `PUT` | `/api/entries/{id}` | Update status (`Reviewed`, `Pending`, `Flagged`) or details |
| `DELETE` | `/api/entries/{id}` | Delete entry record |

---

## 🧪 Testing & Quality Assurance

### Run Backend Unit & Integration Tests:
```bash
cd backend
mvn clean test
```

### Build & Verify Frontend Bundle:
```bash
cd frontend
npm run build
```

---

## 🔄 CI/CD Automation

- **Jenkins Pipeline (`Jenkinsfile`)**:
  - `Backend Test`: Executes JUnit test suite.
  - `Backend Build`: Assembles shaded executable fat JAR.
  - `Frontend Install & Build`: Compiles Vite React production bundle.
  - `Smoke & Health Test`: Spawns backend and queries `/api/health`.

- **GitHub Actions (`.github/workflows/ci.yml`)**:
  - Runs automated CI on every push and pull request to `main` branch.

---

## 📁 Repository Structure

```
.
├── .github/workflows/
│   └── ci.yml               # GitHub Actions CI pipeline
├── backend/
│   ├── Dockerfile           # Multi-stage Java build & runtime container
│   ├── pom.xml              # Maven dependencies & shaded JAR configuration
│   └── src/
│       ├── main/java/com/foodwaste/tracker/
│       │   ├── Application.java            # REST API Server & router
│       │   ├── handler/CorsHandler.java    # CORS & JSON dispatcher
│       │   ├── model/FoodWasteEntry.java   # Waste entry entity model
│       │   ├── model/StatsSummary.java     # Dashboard telemetry model
│       │   └── repository/FoodWasteRepository.java # Thread-safe data store
│       └── test/java/com/foodwaste/tracker/
│           └── ApplicationTest.java        # Comprehensive test suite
├── frontend/
│   ├── Dockerfile           # Multi-stage Node build & Nginx container
│   ├── nginx.conf           # Production Nginx reverse proxy configuration
│   ├── package.json         # Frontend dependencies & scripts
│   ├── vite.config.js       # Vite build & /api proxy configuration
│   └── src/
│       ├── App.jsx          # Main reactive dashboard UI
│       ├── components/
│       │   ├── AlertsBanner.jsx   # DevOps & waste incident telemetry
│       │   ├── Navbar.jsx         # Sticky header with live health badge
│       │   ├── NewEntryModal.jsx  # Waste entry submission dialog
│       │   └── StatCard.jsx       # Interactive KPI metrics card
│       └── index.css        # Tailwind CSS styles
├── docker-compose.yml       # Multi-container orchestration
├── Jenkinsfile              # Jenkins CI/CD pipeline definition
├── package.json             # Root runner scripts
├── README.md                # Project documentation
└── start.sh                 # Unified stack launcher script
```
