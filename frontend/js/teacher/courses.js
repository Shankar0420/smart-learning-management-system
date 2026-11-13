/**
 * Smart Learning Management System
 * Teacher Courses Management Controller
 * Connects to GET /api/courses, DELETE /api/courses/{id}
 */

let teacherCourses = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "courses");

  await loadCourses();
});

async function loadCourses() {
  const container = document.getElementById("teacher-courses-container");
  const countBadge = document.getElementById("courses-count-badge");
  const errorArea = document.getElementById("teacher-courses-error");
  errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/courses").catch(async () => {
      return await window.api.get("/api/teacher/courses");
    });

    teacherCourses = Array.isArray(res) ? res : (res.courses || []);
    countBadge.innerText = `${teacherCourses.length} Course${teacherCourses.length === 1 ? '' : 's'}`;

    if (teacherCourses.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Courses Published",
        "You haven't created any courses yet. Get started by creating your first course offering.",
        '<a href="/teacher/course-form.html" class="btn btn-primary btn-sm">+ Create New Course</a>'
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
            ${teacherCourses.map(c => `
              <tr>
                <td><strong>${window.common.escapeHTML(c.title || "Untitled")}</strong></td>
                <td class="text-muted" style="max-width: 320px;">
                  ${window.common.escapeHTML(c.description || "-")}
                </td>
                <td>
                  <span class="badge ${c.active !== false ? 'badge-success' : 'badge-neutral'}">
                    ${c.active !== false ? 'Active' : 'Draft'}
                  </span>
                </td>
                <td>
                  <div class="flex items-center gap-2">
                    <a href="/teacher/course-details.html?id=${c.id}" class="btn btn-primary btn-sm">
                      Manage Curriculum
                    </a>
                    <a href="/teacher/course-form.html?id=${c.id}" class="btn btn-secondary btn-sm">
                      Edit
                    </a>
                    <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteCourse('${c.id}', '${window.common.escapeHTML(c.title)}')">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadCourses());
  }
}

window.handleDeleteCourse = function(courseId, courseTitle) {
  window.common.confirmAction(`Are you sure you want to permanently delete the course "${courseTitle}"? All associated modules, lessons, quizzes, and assignments will be removed.`, async () => {
    try {
      await window.api.delete(`/api/courses/${courseId}`);
      window.common.showToast("Course deleted successfully!", "success");
      await loadCourses();
    } catch (error) {
      window.common.showToast(error.message || "Failed to delete course", "error");
    }
  });
};
