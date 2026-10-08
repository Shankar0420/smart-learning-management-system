# Smart Learning Management System (LMS) - Frontend

**Galgotias University · Java Programming Project Based Evaluation**

A modern, responsive, and production-grade Learning Management System frontend built using pure **HTML5, CSS3, and Vanilla JavaScript (ES6+)**. This frontend is designed as a standalone client application communicating exclusively through REST APIs with the completed **Java Spring Boot + Spring Security + JPA + MariaDB (`lms_db`)** backend.

---

## 1. Technology Stack

- **Markup:** Semantic HTML5
- **Styling:** Modern CSS3 (CSS Variables, Flexbox, CSS Grid, Responsive Design)
- **Scripting:** Pure Vanilla JavaScript (ES6+ Modules, Fetch API, Async/Await)
- **Backend Architecture:** Java 17+, Spring Boot, Spring Web, Spring Data JPA, Spring Security, Jakarta Validation
- **Database:** MariaDB (`lms_db`)
- **Authentication:** Stateless JWT Bearer Token (`Authorization: Bearer <token>`)
- **Zero Frontend Frameworks:** No React, No Angular, No Vue, No Bootstrap, No Tailwind CSS, No jQuery, No Node.js runtime required to serve static assets.

---

## 2. Folder Structure

```
├── index.html                      # University Portal Landing & Gateway
├── README.md                       # Complete documentation & startup guide
│
├── pages/
│   ├── login.html                  # University Sign In (Role detection & JWT storage)
│   └── register.html               # Student Registration form
│
├── student/
│   ├── dashboard.html              # Student Dashboard (Statistics, progress & grades)
│   ├── courses.html                # My Enrolled Courses & Course Catalog
│   ├── course-details.html         # Course overview & syllabus syllabus outline
│   ├── learning.html               # Interactive Classroom & lesson completion
│   ├── quiz.html                   # Assessment MCQ Quiz runner (score computed by backend)
│   ├── assignments.html            # Course Assignments & text submissions
│   ├── grades.html                 # Student Gradebook (read-only)
│   └── profile.html                # Student Profile details
│
├── teacher/
│   ├── dashboard.html              # Teacher Dashboard (Courses, students & pending items)
│   ├── courses.html                # Teacher Course List with Manage/Delete controls
│   ├── course-form.html            # Create & Edit Course form
│   ├── course-details.html         # Curriculum Hub: Module & Lesson CRUD operations
│   ├── quiz-management.html        # Quiz Builder: MCQs, options, marks & correct key
│   ├── assignment-management.html  # Assignment Creation & Deadline configuration
│   ├── submissions.html            # Student Submissions evaluation & grading interface
│   └── profile.html                # Faculty Profile details
│
├── admin/
│   ├── dashboard.html              # Global University Dashboard & Metrics
│   ├── users.html                  # User Accounts list & Activate/Deactivate controls
│   ├── courses.html                # Supervised Global Course Catalog
│   └── profile.html                # Administrator Superuser Profile
│
├── css/
│   ├── style.css                   # Design tokens, resets, base typography & layout
│   ├── auth.css                    # Authentication split-screen cards & forms
│   ├── dashboard.css               # Sidebar, header, stat cards & learning layouts
│   ├── components.css              # Buttons, cards, badges, modals, toasts & spinners
│   └── responsive.css              # Breakpoints for desktop, laptop, tablet & mobile
│
├── js/
│   ├── config.js                   # Central Backend URL config & health monitor
│   ├── api.js                      # Fetch API wrapper with Bearer token & status handling
│   ├── auth.js                     # Authentication state, login, logout & role guards
│   ├── common.js                   # Layout injector, modals, toasts & UI helpers
│   ├── student/                    # Dedicated student page controllers
│   ├── teacher/                    # Dedicated teacher page controllers
│   └── admin/                      # Dedicated admin page controllers
│
└── assets/
    └── images/                     # Academic hero photography & UI visual assets
```

---

## 3. Backend REST API URL Configuration

The application features a single central configuration in `js/config.js`:

```javascript
const DEFAULT_API_BASE_URL = "http://localhost:8080";
```

### Changing the Backend URL:
1. **Via Code:** Open `js/config.js` and edit `DEFAULT_API_BASE_URL`.
2. **Via User Interface:** Click the **API: http://localhost:8080** badge in the top navigation bar or portal landing page to open the **Backend API Configuration** dialog. Enter the target URL (e.g., `http://localhost:8080` or `http://127.0.0.1:8080`), click **Test Connection**, and save. The URL is saved in `localStorage` across page visits.

---

## 4. How to Start Backend

1. **Verify MariaDB Database:**
   Ensure MariaDB is running and the database `lms_db` is created:
   ```sql
   CREATE DATABASE IF NOT EXISTS lms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. **Configure `application.properties` / `application.yml`:**
   ```properties
   spring.datasource.url=jdbc:mariadb://localhost:3306/lms_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   server.port=8080
   ```
3. **Run Spring Boot Application:**
   ```bash
   mvn clean spring-boot:run
   ```
   The backend will start and expose REST endpoints on `http://localhost:8080`.

---

## 5. How to Start Frontend

### Option A: Using VS Code Live Server (No Node.js Required)
1. Open the project folder in **Visual Studio Code**.
2. Install the **Live Server** extension (by Ritwick Dey).
3. Right-click on `index.html` and choose **"Open with Live Server"**.
4. The frontend will launch in your browser at `http://127.0.0.1:5500/index.html`.

### Option B: Using Any Static Web Server (Python, Nginx, Apache)
```bash
# Python 3
python -m http.server 5500

# Open in browser:
http://localhost:5500/index.html
```

### Option C: Using Vite Development Server
```bash
npm run dev
# Running on http://localhost:3000
```

---

## 6. How Frontend Connects to Backend

1. **Stateless JWT Flow:**
   - User submits credentials to `POST /api/auth/login`.
   - The Spring Boot backend responds with a JWT token and user entity.
   - The token is securely stored in browser `localStorage`.
   - All subsequent calls through `window.api` attach the header:
     ```http
     Authorization: Bearer <token>
     ```
2. **HTTP Status Handling:**
   - `200/201`: Success, returns parsed response data.
   - `400`: Shows clean validation errors returned by backend.
   - `401`: Token expired or invalid; redirects to `/pages/login.html`.
   - `403`: Access denied / forbidden error.
   - `404`: Resource not found notice.
   - `409`: Conflict (e.g. duplicate course enrollment handled gracefully).
   - `500`: Friendly internal server error without leaking database traces.
   - Network failure: Clear banner indicating backend is unreachable at `http://localhost:8080`.

---

## 7. Role-Based Access Control

The frontend routes and dashboard features are strictly partitioned:

| Role | Dashboard | Scope & Permissions |
|---|---|---|
| **STUDENT** | `/student/dashboard.html` | Browse courses, enroll, read lesson content, mark lessons complete, submit quizzes, submit assignments, view personal gradebook. |
| **TEACHER** | `/teacher/dashboard.html` | Create/edit courses, manage syllabus modules & lessons, create quizzes with questions & correct keys, publish assignments, grade student submissions. |
| **ADMIN** | `/admin/dashboard.html` | View university enrollment stats, view & activate/deactivate user accounts, inspect global course catalog. |

*Note: Frontend checks manage UI navigation; Spring Security remains the authoritative security boundary.*

---

## 8. Academic Evaluation Flow & Testing

1. **Student Registration & Login:**
   - Register new student at `/pages/register.html`.
   - Login at `/pages/login.html` $\rightarrow$ automatically routes to `/student/dashboard.html`.
2. **Course Enrollment & Classroom:**
   - Browse catalog at `/student/courses.html`, view details, and click **Enroll**.
   - Open `/student/learning.html` to study lessons and click **Mark as Completed** to update real progress from MariaDB.
3. **Assessment Quiz & Assignment:**
   - Take quiz at `/student/quiz.html`. Correct answers are never revealed during test. Backend evaluates score upon submission.
   - Submit assignment at `/student/assignments.html`.
4. **Teacher Evaluation:**
   - Sign in as teacher $\rightarrow$ `/teacher/dashboard.html`.
   - Create course, modules, lessons, quizzes, and assignments.
   - Review and award marks with feedback at `/teacher/submissions.html`.
5. **Admin Supervision:**
   - Sign in as admin $\rightarrow$ `/admin/dashboard.html`.
   - Activate/deactivate accounts at `/admin/users.html`.
