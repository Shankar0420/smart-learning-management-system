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

## Requirements

Before running the project locally, make sure the required software is installed:

- Java
- Apache Maven
- MariaDB
- Git
- A modern web browser
- Visual Studio Code or another suitable code editor

## Running the Backend

Open a terminal and move into the backend directory:

```bash
cd backend
```

Then start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows, use:

```bash
mvnw.cmd spring-boot:run
```

Make sure MariaDB is running and the database configuration is correctly configured before starting the backend.

## Running the Frontend

Open the `frontend` folder in Visual Studio Code.

Run the frontend using a local development server such as the Live Server extension or another suitable static web server.

Make sure the frontend API configuration points to the running Spring Boot backend.

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
