# ResolveIT – Enterprise IT Incident Management System

ResolveIT is a professional, enterprise-grade IT Service Desk and Incident Management System designed to mirror real-world corporate IT support operations. It enables employees to report workplace technology issues, allows IT support engineers to triage, troubleshoot, and resolve incidents, and provides administrators with oversight over workforce assignments, user accounts, departments, and analytics.

---

## Architecture Overview

ResolveIT follows a clean, layered Spring Boot architecture with complete separation of concerns:

```
Browser (Server-Side Rendered Thymeleaf UI + Bootstrap 5.3)
   ↓
Spring MVC Controllers (Role-based access: Auth, Admin, Employee, Support, Error)
   ↓
Service Layer (Business rules, state machine validation, audit trails, DTO mapping)
   ↓
Repository Layer (Spring Data JPA, JpaSpecificationExecutor for dynamic filtering)
   ↓
JPA / Hibernate 6
   ↓
MySQL Relational Database (resolveit_db)
```

- **Zero JavaScript Requirement**: All core application workflows, form submissions, and state transitions are executed via standard server-side rendering (Thymeleaf and Spring MVC).
- **Security-First**: BCrypt password hashing, session-based authentication, CSRF form protection, and strict role-based authorization filters.
- **Audit Trails**: Complete chronological audit history recorded on every state change, assignment, priority change, and comment.

---

## Technology Stack

- **Platform & Language**: Java 21 LTS
- **Framework**: Spring Boot 3.4.3
- **Security**: Spring Security 6 (Session auth, BCrypt, Method Security, CSRF)
- **Data Persistence**: Spring Data JPA & Hibernate 6 with MySQL 8.0+ / 9.5
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, in-memory H2 database
- **View Engine**: Thymeleaf with `thymeleaf-extras-springsecurity6`
- **UI Framework**: Bootstrap 5.3 + Bootstrap Icons
- **API Documentation**: SpringDoc OpenAPI 2.8+ (Swagger UI)
- **Build Tool**: Apache Maven 3.9+
- **Containerization**: Docker & Docker Compose

---

## User Roles & Permissions

| Role | Default Username | Default Password | Default Landing Page | Key Capabilities |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` / `admin@resolveit.com` | `Admin@123` | `/admin/dashboard` | Global dashboard, user management, department management, assign tickets to support engineers, change priority, reopen closed tickets, view audit trails, view analytical reports. |
| **IT_SUPPORT** | `support` / `support@resolveit.com` | `Support@123` | `/support/dashboard` | Support workbench, view assigned queue, start work (`IN_PROGRESS`), provide resolution notes (`RESOLVED`), add technical updates. |
| **EMPLOYEE** | `employee` / `employee@resolveit.com` | `Employee@123` | `/employee/dashboard` | Personal dashboard, report incidents (`INC-xxxxxx`), view own tickets, add comments, confirm resolution and close tickets (`CLOSED`), reopen tickets if issue persists. |

*Additional Seed Accounts for Demonstration:*
- IT Support Engineer 2: `alex_support` / `alex.support@resolveit.com` (`Support@123`)
- Employee 2: `sarah_hr` / `sarah.hr@resolveit.com` (`Employee@123`)

---

## Incident Workflow State Machine

ResolveIT enforces strict, sensible status transitions to ensure procedural integrity:

```
    [ Employee Reports Incident ]
                 ↓
               OPEN
                 ↓ (Admin assigns to Support Engineer)
              ASSIGNED
                 ↓ (Support Engineer clicks "Start Work")
            IN_PROGRESS
                 ↓ (Support Engineer resolves ticket with resolution notes)
              RESOLVED
                 ↓ (Employee confirms resolution and closes ticket)
               CLOSED
```

- **Reopening Flow**:
  - Employee can reopen a `RESOLVED` ticket back to `IN_PROGRESS` if the issue persists.
  - Admin can reopen a `CLOSED` ticket back to `OPEN` if administrative review warrants it.
- **Business Validation**: An incident cannot transition to `RESOLVED` without detailed resolution notes (minimum 10 characters).

---

## Getting Started

### Prerequisites

- Java 21 JDK or higher
- Maven 3.9+
- MySQL Server 8.0+ or Docker Desktop

### 1. Database Configuration

ResolveIT supports standard environment variables for database connectivity. No sensitive production secrets are stored in Git.

| Environment Variable | Description | Default Development Value |
| :--- | :--- | :--- |
| `DB_HOST` | Database Host | `localhost` |
| `DB_PORT` | Database Port | `3306` |
| `DB_NAME` | Database Name | `resolveit_db` |
| `DB_USERNAME` | Database Username | `root` |
| `DB_PASSWORD` | Database Password | *(empty / provided via env)* |

*For local developer convenience, an uncommitted `src/main/resources/application-local.properties` file (listed in `.gitignore`) can be used to specify local credentials.*

### 2. Running the Application Locally

```bash
# Set your local database password in the shell (or use application-local.properties)
$env:DB_PASSWORD="your_password"   # Windows PowerShell
# or export DB_PASSWORD="your_password" # Linux / macOS

# Start with Maven
mvn spring-boot:run
```

Once started, open your browser at:
**[http://localhost:8080](http://localhost:8080)**

### 3. Running with Packaged JAR

```bash
mvn clean package -DskipTests=false
java -jar target/resolveit-1.0.0.jar
```

### 4. Running with Docker Compose

To spin up both MySQL and ResolveIT in isolated containers:

```bash
docker compose up --build
```

---

## REST API & Swagger Documentation

ResolveIT exposes OpenAPI/Swagger documentation for its REST endpoints:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Key Endpoints:
- `GET /api/v1/incidents` - Paginated incident search and filtering
- `GET /api/v1/incidents/{id}` - Complete details for a specific incident
- `GET /api/v1/incidents/number/{incidentNumber}` - Lookup incident by human-readable ID
- `GET /api/v1/stats` - High-level operational metrics

---

## Automated Testing

ResolveIT features a comprehensive suite of unit and integration tests using JUnit 5, Mockito, and Spring Boot Test with an isolated in-memory H2 database:

```bash
mvn clean test
```

### Verified Test Suites:
- `IncidentServiceTest`: Status transition state machine, automatic sequence number generation, resolution notes validation, authorization checks.
- `UserServiceTest`: BCrypt password encoding, username and email uniqueness constraints.
- `SecurityAccessTest`: Role-based URL filters, unauthorized redirection to `/login`, and 403 Forbidden enforcement.
- `IncidentWorkflowIntegrationTest`: End-to-end multi-actor workflow test verifying reporting, assignment, work initiation, collaboration comments, resolution, and closure in the database.

---

## Project Package Structure

```
com.resolveit
├── ResolveItApplication.java        # Main Spring Boot launcher
├── config
│   ├── DataInitializer.java        # Seed data runner (users, depts, tickets)
│   └── OpenApiConfig.java          # Swagger OpenAPI documentation setup
├── controller
│   ├── AdminController.java        # Administration endpoints
│   ├── AuthController.java         # Login and access routing
│   ├── EmployeeController.java     # Employee self-service endpoints
│   ├── SupportController.java      # IT Support engineer endpoints
│   └── IncidentRestController.java # REST API endpoints
├── dto                             # Request/response data transfer objects
├── entity                          # JPA database entities (User, Incident, etc.)
├── enums                           # Type-safe status, category, priority enums
├── exception
│   ├── GlobalExceptionHandler.java # Centralized error handling
│   └── ResourceNotFoundException.java, etc.
├── repository                      # Spring Data JPA repositories
├── security
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── CustomAuthenticationSuccessHandler.java
│   └── SecurityConfig.java
├── service
│   ├── impl                        # Service business logic implementations
│   └── IncidentService.java, etc.
└── util
    └── SecurityUtils.java
```

---

## Stopping and Restarting

- **To Stop**: Press `Ctrl + C` in the terminal running the application.
- **To Restart**: Run `mvn spring-boot:run` or execute `java -jar target/resolveit-1.0.0.jar`.
