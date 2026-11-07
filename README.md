# Smart Learning Management System

A full stack Learning Management System built to provide a centralized digital platform for students, teachers, and administrators.

The project combines a responsive frontend with a Java Spring Boot backend and MariaDB database. It provides role based access and supports important academic activities such as courses, quizzes, assignments, grades, authentication, and learning management.

## Overview

The Smart Learning Management System is designed to make academic management easier through a single web based platform.

The system supports three major user roles:

- Student
- Teacher
- Administrator

Each role has access to features according to its responsibilities.

## Key Features

### Student

- Secure login and authentication
- Student dashboard
- View and access courses
- Course related learning activities
- Attempt quizzes
- View assignments
- View grades and academic information

### Teacher

- Secure teacher login
- Teacher dashboard
- Create and manage courses
- Manage quizzes
- Manage assignments
- Manage student related academic activities
- Manage grades

### Administrator

- Secure administrator access
- User management
- Student management
- Teacher management
- Course management
- System administration

## Technology Stack

### Frontend

- HTML5
- CSS3
- JavaScript

### Backend

- Java
- Spring Boot
- Spring Security
- REST APIs
- JSON Web Token authentication

### Database

- MariaDB

### Development and Version Control

- Visual Studio Code
- Apache Maven
- Git
- GitHub

## System Architecture

The application follows a full stack architecture:

```text
User
  |
  v
Frontend
HTML + CSS + JavaScript
  |
  | REST API Requests
  v
Backend
Java + Spring Boot
  |
  v
Spring Security
JWT Authentication
  |
  v
MariaDB Database
```

The frontend communicates with the backend through REST API endpoints. The backend handles business logic, authentication, authorization, and database operations.

## Project Structure

```text
smart-learning-management-system/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── ...
│
├── frontend/
│   ├── css/
│   ├── js/
│   ├── pages/
│   ├── index.html
│   └── ...
│
├── .gitignore
└── README.md
```

## Backend Structure

The backend is developed with Spring Boot and follows a layered application structure.

Typical components include:

```text
backend/
└── src/
    └── main/
        ├── java/
        │   └── ...
        │       ├── config/
        │       ├── controller/
        │       ├── dto/
        │       ├── entity/
        │       ├── exception/
        │       ├── repository/
        │       ├── security/
        │       └── service/
        │
        └── resources/
            ├── application.properties
            ├── schema.sql
            └── data.sql
```

The exact package structure may change as the project develops.

## Frontend Structure

The frontend is built using HTML, CSS, and JavaScript.

JavaScript is responsible for:

- API communication
- Authentication handling
- User interactions
- Course operations
- Quiz operations
- Assignment operations
- Dashboard functionality

Example API request:

```javascript
fetch("/api/courses")
```

The actual API base URL depends on the backend configuration and deployment environment.

## Authentication and Security

The backend uses Spring Security and JSON Web Token based authentication.

Authentication allows users to securely log in, while role based authorization controls access to protected resources.

The application is designed around role based access for:

```text
STUDENT
TEACHER
ADMINISTRATOR
```

Sensitive configuration files and credentials should not be committed to the GitHub repository.

## Database

MariaDB is used for persistent data storage.

The database can contain information related to:

- Users
- Roles
- Courses
- Quizzes
- Assignments
- Grades
- Authentication
- Learning activities

Database configuration is maintained through the Spring Boot application configuration.

## Local Quick Start (macOS / VS Code)

You can start the complete LMS (Database check + Spring Boot Backend + Frontend + Browser) with a single command:

```bash
# Start complete LMS locally
./start-lms.sh
```

To stop all local services:

```bash
# Stop all local LMS services
./stop-lms.sh
```

### Local URLs
| Component | Local URL | Description |
|---|---|---|
| **Frontend UI** | [http://localhost:5500](http://localhost:5500) | Main LMS portal (HTML/CSS/Vanilla JS) |
| **Backend REST API** | [http://localhost:8080](http://localhost:8080) | Spring Boot REST API |
| **API Health Check** | [http://localhost:8080/api/health](http://localhost:8080/api/health) | Returns `{"status":"UP", ...}` |

### Pre-Seeded Local Accounts
| Role | Email | Password | Professional ID | Notes |
|---|---|---|---|---|
| **Administrator** | `admin@lms.com` | `admin123` | N/A | Full administrative privileges |
| **Teacher** | `teacher@lms.com` | `teacher123` | `GUL-TEA-2026-001` | Course, quiz, assignment management |
| **Student** | `student1@lms.com` | `student123` | `GUL-STU-2026-001` | Enrolled in Java Masterclass |
| **Student** | `student2@lms.com` | `student123` | `GUL-STU-2026-002` | Enrolled student |

---

## Running in Visual Studio Code

1. Open the repository root directory in VS Code:
   ```bash
   code .
   ```
2. Recommended VS Code extensions (auto-prompted via `.vscode/extensions.json`):
   - **Extension Pack for Java** (`vscjava.vscode-java-pack`)
   - **Spring Boot Extension Pack** (`vmware.vscode-spring-boot`)
   - **Live Server** (`ritwickdey.liveserver`)
3. Run or debug via VS Code:
   - Press `Cmd + Shift + P` -> **Tasks: Run Task** -> **Start Complete LMS**
   - Or run the backend with debugger: Press `F5` -> Select **LMS Backend (Spring Boot)**
   - Or right-click `frontend/index.html` -> **Open with Live Server** (Port 5500)

---

## Manual Execution Commands

### 1. Database Setup (Local MySQL / MariaDB)
Ensure MySQL is running on `localhost:3306`:
```bash
# Check status / start via Homebrew
brew services start mysql
```
The application connects automatically to `jdbc:mariadb://localhost:3306/lms_db` with user `root` (no password by default). All 16 database tables and migrations are pre-configured.

### 2. Running the Backend
From the repository root:
```bash
./start-backend.sh
```
Or directly with Maven:
```bash
mvn -f backend/pom.xml spring-boot:run
```

### 3. Running the Frontend
From the repository root:
```bash
./start-frontend.sh
```
Or with Python:
```bash
python3 -m http.server 5500 --directory frontend
```

### 4. Running Backend Tests
Execute all 57 integration and unit tests against an isolated in-memory test database:
```bash
mvn -f backend/pom.xml test
```

## Connecting Frontend and Backend

The frontend communicates with the backend using HTTP requests to REST API endpoints.

For example:

```text
Frontend
   |
   | GET /api/courses
   v
Spring Boot Backend
   |
   v
MariaDB
```

The backend processes the request, performs the required database operation, and sends a response back to the frontend.

## Git and GitHub

The project uses Git for version control and GitHub for remote repository management.

Repository:

https://github.com/Shankar0420/smart-learning-management-system

To clone the project:

```bash
git clone https://github.com/Shankar0420/smart-learning-management-system.git
```

Then:

```bash
cd smart-learning-management-system
```

## Development Workflow

A typical development workflow is:

```text
Make changes
     |
     v
Test locally
     |
     v
git status
     |
     v
git add .
     |
     v
git commit -m "Describe your changes"
     |
     v
git push
```

Always review the files shown by `git status` before pushing changes.

## Important Security Notes

Do not upload sensitive information to GitHub.

Never commit:

- Database passwords
- API keys
- Private credentials
- Secret tokens
- Local environment secrets
- Personal configuration containing sensitive information

Use `.gitignore` and environment based configuration where appropriate.

## Future Enhancements

Possible future improvements include:

- Online video lectures
- Attendance management
- Student progress tracking
- Notifications
- Email notifications
- Advanced analytics
- Certificate generation
- Improved course content management
- Cloud deployment
- Automated testing
- Improved user interface

## Project Purpose

This project demonstrates practical implementation of a full stack Learning Management System using frontend development, backend development, REST APIs, database management, authentication, authorization, and Git based version control.

It is intended as an academic and learning project and can be extended into a production ready learning platform with additional testing, deployment, monitoring, and security improvements.

## Author

**Shankar Kumar Chaudhary**

B.Tech Student

GitHub:

https://github.com/Shankar0420

## License

This project is currently intended for educational and development purposes.
