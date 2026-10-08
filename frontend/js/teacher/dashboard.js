/**
 * Smart Learning Management System
 * Teacher Dashboard Controller
 * Connects to GET /api/teacher/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "dashboard");

  const user = window.auth.getCurrentUser();
  if (user && user.name) {
    document.getElementById("teacher-welcome-heading").innerText = `Faculty Portal · Professor ${user.name}`;
  }

  await loadTeacherDashboard();
});

async function loadTeacherDashboard() {
  const statCourses = document.getElementById("stat-teacher-courses");
  const statStudents = document.getElementById("stat-teacher-students");
  const statSubmissions = document.getElementById("stat-teacher-submissions");
  const coursesContainer = document.getElementById("teacher-recent-courses");
  const errorArea = document.getElementById("teacher-error-area");
  errorArea.classList.add("hidden");

  try {
    const data = await window.api.get("/api/teacher/dashboard");

    // All dynamic values strictly from backend
    statCourses.innerText = data.totalCourses ?? data.coursesCount ?? 0;
    statStudents.innerText = data.totalStudents ?? data.studentsCount ?? 0;
    statSubmissions.innerText = data.pendingSubmissions ?? data.pendingSubmissionsCount ?? 0;

    // Render courses table or list if provided
    const courses = data.courses || [];
    if (courses.length === 0) {
      // Query /api/courses
      const allCourses = await window.api.get("/api/courses").catch(() => []);
      renderCoursesSummary(Array.isArray(allCourses) ? allCourses : (allCourses.courses || []));
    } else {
      renderCoursesSummary(courses);
    }
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(coursesContainer, error.message, () => loadTeacherDashboard());
  }
}

function renderCoursesSummary(courses) {
  const container = document.getElementById("teacher-recent-courses");
  if (!container) return;

  if (courses.length === 0) {
    window.common.renderEmptyState(
      container,
      "No Courses Created",
      "You have not published any courses yet.",
      '<a href="/teacher/course-form.html" class="btn btn-primary btn-sm">+ Create Your First Course</a>'
    );
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>Course Title</th>
            <th>Description</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${courses.slice(0, 5).map(c => `
            <tr>
              <td><strong>${window.common.escapeHTML(c.title || "Untitled")}</strong></td>
              <td class="text-muted" style="max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                ${window.common.escapeHTML(c.description || "-")}
              </td>
              <td>
                <span class="badge ${c.active !== false ? 'badge-success' : 'badge-neutral'}">
                  ${c.active !== false ? 'Active' : 'Draft'}
                </span>
              </td>
              <td>
                <div class="flex gap-2">
                  <a href="/teacher/course-details.html?id=${c.id}" class="btn btn-secondary btn-sm">Manage</a>
                  <a href="/teacher/course-form.html?id=${c.id}" class="btn btn-outline btn-sm">Edit</a>
                </div>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}
