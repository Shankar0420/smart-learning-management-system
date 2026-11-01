/**
 * Smart Learning Management System
 * Admin Course Catalog Controller
 * Connects to GET /api/courses, DELETE /api/courses/{id}
 */

let allCourses = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "courses");

  await loadAdminCourses();
});

async function loadAdminCourses() {
  const container = document.getElementById("admin-courses-container");
  const countBadge = document.getElementById("admin-courses-count");
  const errorArea = document.getElementById("admin-courses-error");
  errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/courses").catch(async () => {
      return await window.api.get("/api/admin/courses");
    });

    allCourses = Array.isArray(res) ? res : (res.courses || []);
    countBadge.innerText = `${allCourses.length} Course${allCourses.length === 1 ? '' : 's'}`;

    if (allCourses.length === 0) {
      window.common.renderEmptyState(container, "No Courses Found", "No courses have been published in the database yet.");
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Course Title</th>
              <th>Teacher / Faculty</th>
              <th>Status</th>
              <th>Modules</th>
              <th>Created Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${allCourses.map(c => {
              const id = c.id;
              const title = c.title || "Untitled Course";
              const teacher = c.teacherName || c.teacher?.name || "Faculty Lead";
              const isActive = c.active !== false;
              const modulesCount = (c.modules || []).length;
              const date = c.createdAt || c.createdDate;

              return `
                <tr>
                  <td>
                    <strong>${window.common.escapeHTML(title)}</strong>
                    <div class="text-muted" style="font-size: 0.75rem; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                      ${window.common.escapeHTML(c.description || "")}
                    </div>
                  </td>
                  <td>👨‍🏫 ${window.common.escapeHTML(teacher)}</td>
                  <td>
                    <span class="badge ${isActive ? 'badge-success' : 'badge-neutral'}">
                      ${isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td><span class="badge badge-neutral">${modulesCount} modules</span></td>
                  <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                  <td>
                    <button class="btn btn-danger-outline btn-sm" onclick="handleAdminDeleteCourse('${id}', '${window.common.escapeHTML(title)}')">
                      Delete Course
                    </button>
                  </td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadAdminCourses());
  }
}

window.handleAdminDeleteCourse = function(id, title) {
  window.common.confirmAction(`Are you sure you want to delete course "${title}" from the university catalog?`, async () => {
    try {
      await window.api.delete(`/api/courses/${id}`);
      window.common.showToast("Course removed from catalog successfully!", "success");
      await loadAdminCourses();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete course", "error");
    }
  });
};
