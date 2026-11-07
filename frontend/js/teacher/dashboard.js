/**
 * Galgotias LMS
 * Teacher Dashboard Controller
 * Connects to GET /api/teacher/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "dashboard");

  const user = window.auth.getCurrentUser();
  if (user && user.name) {
    const idSuffix = user.professionalId ? ` (ID: ${user.professionalId})` : "";
    document.getElementById("teacher-welcome-heading").innerText = `Faculty Portal · Professor ${user.name}${idSuffix}`;
  }

  await loadTeacherDashboard();
});

async function loadTeacherDashboard() {
  const statCourses = document.getElementById("stat-teacher-courses");
  const statMaterials = document.getElementById("stat-teacher-materials");
  const statQuizzes = document.getElementById("stat-teacher-quizzes");
  const statAssignments = document.getElementById("stat-teacher-assignments");
  const statStudents = document.getElementById("stat-teacher-students");
  const statSubmissions = document.getElementById("stat-teacher-submissions");
  const coursesContainer = document.getElementById("teacher-recent-courses");
  const materialsContainer = document.getElementById("teacher-recent-materials-container");
  const errorArea = document.getElementById("teacher-error-area");
  errorArea.classList.add("hidden");

  try {
    const data = await window.api.get("/api/teacher/dashboard");

    // All dynamic values strictly from backend
    if (statCourses) statCourses.innerText = data.totalCourses ?? 0;
    if (statMaterials) statMaterials.innerText = data.totalMaterials ?? 0;
    if (statQuizzes) statQuizzes.innerText = data.totalQuizzes ?? 0;
    if (statAssignments) statAssignments.innerText = data.totalAssignments ?? 0;
    if (statStudents) statStudents.innerText = data.totalStudents ?? 0;
    if (statSubmissions) statSubmissions.innerText = data.pendingSubmissions ?? 0;

    // Render Recent Study Materials
    const recentMaterials = data.recentMaterials || [];
    renderRecentMaterials(recentMaterials);

    // Render courses summary
    const allCourses = await window.api.get("/api/courses").catch(() => []);
    renderCoursesSummary(Array.isArray(allCourses) ? allCourses : (allCourses.courses || []));

  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(coursesContainer, error.message, () => loadTeacherDashboard());
  }
}

function renderRecentMaterials(materials) {
  const container = document.getElementById("teacher-recent-materials-container");
  if (!container) return;

  if (materials.length === 0) {
    window.common.renderEmptyState(
      container,
      "No Study Materials Uploaded",
      "Upload PPT, PPTX or PDF materials within your course modules.",
      '<a href="/teacher/courses.html" class="btn btn-primary btn-sm">Go to Courses</a>'
    );
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 80px;">Format</th>
            <th>Title & Description</th>
            <th>Course</th>
            <th>Size</th>
            <th>Uploaded Date</th>
            <th style="text-align: right;">Action</th>
          </tr>
        </thead>
        <tbody>
          ${materials.map(m => {
            const fileType = (m.fileType || "").toUpperCase();
            let badgeClass = "badge-info";
            if (fileType === "PDF") badgeClass = "badge-danger";
            else if (fileType === "PPT" || fileType === "PPTX") badgeClass = "badge-warning";

            const sizeKB = Math.round((m.fileSize || 0) / 1024);
            const sizeStr = sizeKB > 1024 ? (sizeKB / 1024).toFixed(1) + " MB" : sizeKB + " KB";

            return `
              <tr>
                <td><span class="badge ${badgeClass}" style="font-weight: 700;">${fileType || "DOC"}</span></td>
                <td>
                  <strong>${window.common.escapeHTML(m.title)}</strong>
                  ${m.description ? `<p class="text-muted" style="font-size: 0.8rem; margin: 0.2rem 0 0 0;">${window.common.escapeHTML(m.description)}</p>` : ''}
                </td>
                <td class="text-muted">${window.common.escapeHTML(m.courseTitle || "-")}</td>
                <td class="text-muted tabular-nums">${sizeStr}</td>
                <td class="text-muted tabular-nums">${window.common.formatDate(m.createdAt)}</td>
                <td style="text-align: right;">
                  <button class="btn btn-secondary btn-sm" onclick="handleDownloadTeacherMaterial('${m.id}', '${window.common.escapeHTML(m.originalFileName || 'material')}')">
                    ⬇ Download
                  </button>
                </td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
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
            <th style="text-align: right;">Actions</th>
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
              <td style="text-align: right;">
                <div class="flex items-center justify-end gap-2">
                  <a href="/teacher/course-details.html?id=${c.id}" class="btn btn-secondary btn-sm">Manage Curriculum</a>
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

window.handleDownloadTeacherMaterial = async function(materialId, filename) {
  try {
    window.common.showToast("Downloading material...", "info");
    await window.api.download(`/api/materials/${materialId}/download`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download material", "error");
  }
};
