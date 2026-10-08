/**
 * Smart Learning Management System
 * Admin Dashboard Controller
 * Connects to GET /api/admin/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "dashboard");

  await loadAdminDashboard();
});

async function loadAdminDashboard() {
  const statStudents = document.getElementById("stat-admin-students");
  const statTeachers = document.getElementById("stat-admin-teachers");
  const statCourses = document.getElementById("stat-admin-courses");
  const statEnrollments = document.getElementById("stat-admin-enrollments");
  const errorArea = document.getElementById("admin-error-area");
  errorArea.classList.add("hidden");

  try {
    const data = await window.api.get("/api/admin/dashboard");

    // All dynamic values strictly from backend
    statStudents.innerText = data.totalStudents ?? data.studentsCount ?? 0;
    statTeachers.innerText = data.totalTeachers ?? data.teachersCount ?? 0;
    statCourses.innerText = data.totalCourses ?? data.coursesCount ?? 0;
    statEnrollments.innerText = data.totalEnrollments ?? data.enrollmentsCount ?? 0;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
  }
}
