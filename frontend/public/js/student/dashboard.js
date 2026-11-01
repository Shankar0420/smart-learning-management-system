/**
 * Smart Learning Management System
 * Student Dashboard Controller
 * Connects to GET /api/student/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  // Enforce Student Role
  if (!window.auth.requireRole("STUDENT")) return;

  // Initialize Layout
  window.common.initDashboardLayout("STUDENT", "dashboard");

  const user = window.auth.getCurrentUser();
  if (user && user.name) {
    document.getElementById("welcome-heading").innerText = `Welcome back, ${user.name}!`;
  }

  await loadStudentDashboard();
});

async function loadStudentDashboard() {
  const statEnrolled = document.getElementById("stat-enrolled");
  const statActive = document.getElementById("stat-active");
  const statCompleted = document.getElementById("stat-completed");
  const statPending = document.getElementById("stat-pending");
  const progressContainer = document.getElementById("course-progress-container");
  const gradesContainer = document.getElementById("recent-grades-container");
  const assignmentsContainer = document.getElementById("pending-assignments-container");
  const errorArea = document.getElementById("dashboard-error-area");

  try {
    const data = await window.api.get("/api/student/dashboard");

    // Populate Statistics from Backend
    statEnrolled.innerText = data.totalEnrolledCourses ?? data.enrolledCoursesCount ?? 0;
    statActive.innerText = data.activeCourses ?? data.activeCoursesCount ?? 0;
    statCompleted.innerText = data.completedLessons ?? data.completedLessonsCount ?? 0;
    statPending.innerText = data.pendingAssignments ?? data.pendingAssignmentsCount ?? 0;

    // Render Course Progress
    const coursesProgress = data.courseProgress || data.courses || [];
    if (coursesProgress.length === 0) {
      window.common.renderEmptyState(
        progressContainer,
        "No Enrolled Courses",
        "You have not enrolled in any courses yet.",
        '<a href="/student/courses.html" class="btn btn-primary btn-sm">Explore Courses</a>'
      );
    } else {
      progressContainer.innerHTML = coursesProgress.map(cp => {
        const title = cp.courseTitle || cp.title || cp.course?.title || "Course";
        const courseId = cp.courseId || cp.id || cp.course?.id;
        const percent = Math.min(100, Math.max(0, cp.percentage ?? cp.progress ?? 0));
        const completed = cp.completedLessons ?? 0;
        const total = cp.totalLessons ?? 0;

        return `
          <div style="margin-bottom: 1.25rem;">
            <div class="progress-header">
              <strong style="color: var(--dark);">${window.common.escapeHTML(title)}</strong>
              <span>${percent}% (${completed}/${total} lessons)</span>
            </div>
            <div class="progress-bar">
              <div class="progress-bar-fill ${percent === 100 ? 'success' : ''}" style="width: ${percent}%;"></div>
            </div>
            <div style="margin-top: 0.35rem; text-align: right;">
              <a href="/student/learning.html?courseId=${courseId}" style="font-size: 0.75rem;">Continue Learning →</a>
            </div>
          </div>
        `;
      }).join("");
    }

    // Render Recent Grades
    const recentGrades = data.recentGrades || data.grades || [];
    if (recentGrades.length === 0) {
      window.common.renderEmptyState(gradesContainer, "No Grades Available", "You haven't received any assessment grades yet.");
    } else {
      gradesContainer.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Assessment</th>
                <th>Course</th>
                <th>Marks</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              ${recentGrades.map(g => `
                <tr>
                  <td><strong>${window.common.escapeHTML(g.assessmentName || g.title || "Assessment")}</strong></td>
                  <td class="text-muted">${window.common.escapeHTML(g.courseName || g.courseTitle || "-")}</td>
                  <td><span class="badge badge-success">${g.marks}/${g.maxMarks || 100}</span></td>
                  <td class="text-muted">${window.common.formatDate(g.date || g.createdAt)}</td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      `;
    }

    // Render Pending Assignments
    const pendingList = data.pendingAssignmentsList || data.assignments || [];
    if (pendingList.length === 0) {
      window.common.renderEmptyState(assignmentsContainer, "No Pending Assignments", "All assignments are up to date!");
    } else {
      assignmentsContainer.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Assignment</th>
                <th>Course</th>
                <th>Deadline</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${pendingList.map(a => `
                <tr>
                  <td><strong>${window.common.escapeHTML(a.title || "Assignment")}</strong></td>
                  <td class="text-muted">${window.common.escapeHTML(a.courseName || a.course?.title || "-")}</td>
                  <td class="text-warning">${window.common.formatDate(a.deadline)}</td>
                  <td>
                    <a href="/student/assignments.html?assignmentId=${a.id}" class="btn btn-primary btn-sm">Submit</a>
                  </td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      `;
    }

  } catch (error) {
    errorArea.className = "alert alert-danger";
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");

    window.common.renderErrorState(progressContainer, error.message, () => loadStudentDashboard());
    window.common.renderErrorState(gradesContainer, error.message);
    window.common.renderErrorState(assignmentsContainer, error.message);
  }
}
