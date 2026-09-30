# ResolveIT – Enterprise IT Incident Management System

ResolveIT is a professional, enterprise-grade IT Service Desk and Incident Management System designed to mirror real-world corporate IT support operations. It enables employees to report workplace technology issues, allows IT support engineers to triage, troubleshoot, and resolve incidents, and provides administrators with oversight over workforce assignments, user accounts, departments, and analytics.

---

## Live Demo

ResolveIT is deployed and publicly accessible:

- **Live Application:** https://resolveit-kihv.onrender.com
- **Deployment:** Render Docker Web Service
- **Production Database:** TiDB Cloud
- **Application Port:** 8080 inside the container

> Note: The Render Free service may sleep after inactivity. The first request after sleeping may take some time while the service starts.
> The production deployment uses environment variables for database connectivity and does not store production database credentials in the repository.

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
MySQL-Compatible Database
```

> Local development can use a database such as `resolveit_db`. The verified production deployment uses TiDB Cloud with the production database name configured through environment variables.

- **Zero JavaScript Requirement**: All core application workflows, form submissions, and state transitions are executed via standard server-side rendering (Thymeleaf and Spring MVC).
- **Security-First**: BCrypt password hashing, session-based authentication, CSRF form protection, and strict role-based authorization filters.
- **Audit Trails**: Complete chronological audit history recorded on every state change, assignment, priority change, and comment.

---

## Technology Stack

- **Platform & Language**: Java 21 LTS
- **Framework**: Spring Boot 3.4.3
- **Security**: Spring Security 6 (Session auth, BCrypt, Method Security, CSRF)
- **Data Persistence**: Spring Data JPA & Hibernate 6 with MySQL 8.0+
- **Production Database**: TiDB Cloud
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

### 1. Database & Environment Configuration

ResolveIT supports standard environment variables for database connectivity and cloud hosting. No sensitive production secrets are stored in Git.

| Environment Variable | Description | Default Local Value | Cloud / Render Value |
| :--- | :--- | :--- | :--- |
| `PORT` | Web Server HTTP Port | `8080` | Assigned by Render (e.g. `10000`) |
| `DB_HOST` | Database Hostname / IP | `localhost` | External MySQL host (e.g. cloud provider) |
| `DB_PORT` | Database Port | `3306` | External MySQL port (e.g. `3306` or provider port) |
| `DB_NAME` | Database Name | `resolveit_db` | Database name on MySQL host |
| `DB_USERNAME` | Database Username | `root` | Database user account |
| `DB_PASSWORD` | Database Password | *(empty / provided via env)* | Secret database password |
| `DB_SSL_MODE` | Enable SSL for DB connection | `false` | `true` or `REQUIRED` (recommended for cloud DB) |
| `DB_URL` | *(Optional)* Full JDBC URL override | *(none)* | `jdbc:mysql://<host>:<port>/<name>?...` |
| `JAVA_OPTS` | JVM Memory & GC flags | *(container tuned)* | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` |

*For local developer convenience, an uncommitted `src/main/resources/application-local.properties` file (listed in `.gitignore`) can be used to specify local credentials.*

> **Security & Secret Management**:
> - `DB_PASSWORD` must never be committed to Git or hardcoded in any configuration file.
> - `src/main/resources/application-local.properties` remains strictly ignored by Git and Docker.
> - Production database credentials are supplied exclusively through Render environment variables or secret environment groups.
> - No passwords, connection URIs with credentials, or cloud secrets are stored in the repository.

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

### 4. Running with Docker Compose (Local Stack)

To spin up both MySQL and ResolveIT in isolated local containers:

```bash
docker compose up --build
```

### 5. Building & Running the Production Docker Image Locally

```bash
# 1. Build the production Docker image
docker build -t resolveit:latest .

# 2. Run the container with environment variables
docker run -d --name resolveit-container \
  -p 8080:8080 \
  -e PORT=8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_NAME=resolveit_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD="your_password" \
  -e DB_SSL_MODE=false \
  resolveit:latest

# 3. View container logs
docker logs -f resolveit-container
```

---

## Free Cloud Deployment on Render

ResolveIT is configured and successfully deployed using **Render + Docker + TiDB Cloud** as the external production database.

Verified production deployment:

- **Render Service:** `resolveit`
- **Production URL:** `https://resolveit-kihv.onrender.com`
- **Container Runtime:** Docker
- **Application Port:** `8080`
- **Production Database:** TiDB Cloud
- **Production Database Name:** `resolveit_db`
- **Database SSL:** Enabled

### Step 1: Create an External MySQL Database

> For the verified ResolveIT deployment, TiDB Cloud is used as the external production database with `resolveit_db`.

To replicate or set up an external MySQL database, you can provision an instance with any MySQL-compatible cloud provider:
* **TiDB Cloud (Serverless)**: Fully managed, MySQL 8.0-compatible serverless database with native SSL support (used for the verified deployment).
* **Aiven for MySQL**: Cloud MySQL service with automated configuration and SSL support (alternative provider).
* **Clever Cloud**: Cloud MySQL add-on service.

Note down your connection credentials from your chosen provider:
* Host (e.g., your database host domain)
* Port (e.g., `4000` for TiDB Cloud, `3306`, or provider port)
* Database Name (e.g., `resolveit_db`)
* Username & Password
* SSL mode (required for secure remote connections, set `DB_SSL_MODE=true`)

### Step 2: Push Repository to GitHub

Ensure all files are committed and push your repository to your GitHub account:

```bash
git remote add origin https://github.com/<your-username>/resolveit.git
git branch -M main
git push -u origin main
```

*(Note: Secrets and `application-local.properties` are strictly excluded by `.gitignore` and `.dockerignore`)*

### Step 3: Deploy on Render

#### Option A: One-Click Blueprint (Recommended)
1. In the [Render Dashboard](https://dashboard.render.com), click **New +** → **Blueprint**.
2. Select your `resolveit` repository. Render will automatically read `render.yaml`.
3. Provide the database connection details in the prompted Environment Variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE`).
4. Click **Apply**.

#### Option B: Manual Web Service Setup
1. In [Render Dashboard](https://dashboard.render.com), click **New +** → **Web Service**.
2. Connect your GitHub repository.
3. Configure the service:
   * **Name**: `resolveit` (or your preferred name)
   * **Region**: Choose the region closest to your database
   * **Environment**: `Docker`
   * **Plan**: `Free`
4. Add the following **Environment Variables** in the Render settings:
   * `PORT`: `8080` (Render will map this automatically)
   * `DB_HOST`: `<your-cloud-db-host>`
   * `DB_PORT`: `<your-cloud-db-port>`
   * `DB_NAME`: `<your-cloud-db-name>`
   * `DB_USERNAME`: `<your-cloud-db-user>`
   * `DB_PASSWORD`: `<your-cloud-db-password>`
   * `DB_SSL_MODE`: `true`
   * `JAVA_OPTS`: `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
5. Click **Create Web Service**.

Render will clone your repository, build the multi-stage Docker image, start the container, and assign a public HTTPS URL (`https://resolveit-kihv.onrender.com`).

### Step 4: Access and Verify

Once Render displays **Live**:
* Open **`https://resolveit-kihv.onrender.com`** on any smartphone, tablet, or browser.
* Log in using the seeded demonstration credentials:
  * **Admin**: `admin` / `Admin@123`
  * **Support**: `support` / `Support@123`
  * **Employee**: `employee` / `Employee@123`

> These credentials are seeded demonstration accounts intended for portfolio/testing purposes. Production deployments should replace them with secure credentials and appropriate secret management.

* Access interactive API documentation at: **`https://resolveit-kihv.onrender.com/swagger-ui/index.html`**

### Verified Production Deployment

The production deployment has been successfully verified with:

- Docker image build
- Spring Boot startup
- Java 21 runtime
- Tomcat startup on port 8080
- Remote MySQL connectivity to TiDB Cloud (`resolveit_db`)
- HikariCP connection pool initialization
- Hibernate/JPA initialization
- Database schema initialization
- Application startup and public Render availability

---

## REST API & Swagger Documentation

ResolveIT exposes OpenAPI/Swagger documentation for its REST endpoints:

### Local Development Swagger
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Production Swagger
- **Swagger UI:** https://resolveit-kihv.onrender.com/swagger-ui/index.html
- **OpenAPI JSON:** https://resolveit-kihv.onrender.com/v3/api-docs

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
