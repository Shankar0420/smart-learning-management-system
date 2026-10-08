/**
 * Smart Learning Management System
 * Teacher Assignment Management Controller
 * Connects to:
 * POST /api/courses/{id}/assignments
 * PUT /api/assignments/{id}
 * DELETE /api/assignments/{id}
 */

let courseId = null;
let teacherCourses = [];
let assignments = [];
let activeAssignmentId = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "assignments");

  const params = new URLSearchParams(window.location.search);
  courseId = params.get("courseId");

  if (courseId) {
    document.getElementById("assign-back-link").href = `/teacher/course-details.html?id=${courseId}`;
  }

  setupAssignmentForm();
  await loadData();
});

async function loadData() {
  const container = document.getElementById("assignments-list-container");
  const errorArea = document.getElementById("assign-mgmt-error");
  errorArea.classList.add("hidden");

  try {
    // Load courses to populate selector
    const coursesRes = await window.api.get("/api/courses").catch(async () => {
      return await window.api.get("/api/teacher/courses");
    });
    teacherCourses = Array.isArray(coursesRes) ? coursesRes : (coursesRes.courses || []);

    const courseSelect = document.getElementById("assign-course-select");
    courseSelect.innerHTML = teacherCourses.map(c => `
      <option value="${c.id}" ${courseId && String(c.id) === String(courseId) ? "selected" : ""}>
        ${window.common.escapeHTML(c.title || "Course")}
      </option>
    `).join("");

    // Load assignments
    if (courseId) {
      const res = await window.api.get(`/api/courses/${courseId}/assignments`).catch(async () => {
        return await window.api.get("/api/assignments");
      });
      assignments = Array.isArray(res) ? res : (res.assignments || []);
    } else {
      const res = await window.api.get("/api/assignments").catch(() => []);
      assignments = Array.isArray(res) ? res : (res.assignments || []);
    }

    if (assignments.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Assignments Created",
        "Publish assignments with deadlines and evaluation rubrics for your enrolled students.",
        '<button class="btn btn-primary btn-sm" onclick="openCreateAssignmentModal()">+ Create Assignment</button>'
      );
      return;
    }

    renderAssignmentsList();
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadData());
  }
}

function renderAssignmentsList() {
  const container = document.getElementById("assignments-list-container");

  container.innerHTML = `
    <div style="display: flex; flex-direction: column; gap: 1.25rem;">
      ${assignments.map(a => {
        const id = a.id;
        const title = a.title || "Assignment";
        const desc = a.description || "Course assignment.";
        const courseTitle = a.courseTitle || a.course?.title || "Course";
        const deadline = a.deadline;
        const maxMarks = a.maxMarks || a.marks || 100;

        return `
          <div class="card">
            <div class="card-body">
              <div class="flex items-center justify-between flex-wrap gap-3" style="margin-bottom: 0.75rem;">
                <div>
                  <span class="badge badge-info">${window.common.escapeHTML(courseTitle)}</span>
                  <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--dark); margin-top: 0.25rem;">
                    ${window.common.escapeHTML(title)}
                  </h3>
                </div>

                <div class="flex items-center gap-2">
                  <a href="/teacher/submissions.html?assignmentId=${id}" class="btn btn-primary btn-sm">
                    📥 View Submissions
                  </a>
                  <button class="btn btn-secondary btn-sm" onclick='openEditAssignmentModal(${JSON.stringify(a)})'>
                    Edit
                  </button>
                  <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteAssignment('${id}', '${window.common.escapeHTML(title)}')">
                    Delete
                  </button>
                </div>
              </div>

              <p class="text-muted" style="font-size: 0.9rem; line-height: 1.6; margin-bottom: 1.25rem;">
                ${window.common.escapeHTML(desc)}
              </p>

              <div class="flex items-center gap-6 text-muted" style="font-size: 0.85rem; padding-top: 0.75rem; border-top: 1px solid var(--border-color); flex-wrap: wrap;">
                <span>📅 Deadline: <strong class="text-primary">${window.common.formatDate(deadline)}</strong></span>
                <span>💯 Maximum Marks: <strong>${maxMarks}</strong></span>
              </div>
            </div>
          </div>
        `;
      }).join("")}
    </div>
  `;
}

window.openCreateAssignmentModal = function() {
  activeAssignmentId = null;
  document.getElementById("assignment-modal-title").innerText = "Create Assignment";
  document.getElementById("assign-title-input").value = "";
  document.getElementById("assign-desc-input").value = "";
  document.getElementById("assign-marks-input").value = "100";

  // Set default deadline 7 days from now formatted for input
  const d = new Date();
  d.setDate(d.getDate() + 7);
  document.getElementById("assign-deadline-input").value = d.toISOString().slice(0, 16);

  window.common.openModal("assignment-modal");
};

window.openEditAssignmentModal = function(assignment) {
  activeAssignmentId = assignment.id;
  document.getElementById("assignment-modal-title").innerText = "Edit Assignment";
  document.getElementById("assign-title-input").value = assignment.title || "";
  document.getElementById("assign-desc-input").value = assignment.description || "";
  document.getElementById("assign-marks-input").value = assignment.maxMarks || 100;

  if (assignment.deadline) {
    try {
      const d = new Date(assignment.deadline);
      document.getElementById("assign-deadline-input").value = d.toISOString().slice(0, 16);
    } catch (e) {}
  }

  window.common.openModal("assignment-modal");
};

window.handleDeleteAssignment = function(id, title) {
  window.common.confirmAction(`Are you sure you want to delete assignment "${title}"?`, async () => {
    try {
      await window.api.delete(`/api/assignments/${id}`);
      window.common.showToast("Assignment deleted successfully!", "success");
      await loadData();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete assignment", "error");
    }
  });
};

function setupAssignmentForm() {
  document.getElementById("assignment-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("assign-title-input").value.trim();
    const description = document.getElementById("assign-desc-input").value.trim();
    const deadline = document.getElementById("assign-deadline-input").value;
    const maxMarks = Number(document.getElementById("assign-marks-input").value) || 100;
    const selectedCourse = document.getElementById("assign-course-select").value || courseId;

    if (!title || !description || !deadline) {
      window.common.showToast("Please fill in all required fields.", "error");
      return;
    }

    const btn = document.getElementById("save-assignment-btn");
    btn.disabled = true;

    const payload = {
      title,
      description,
      deadline,
      maxMarks
    };

    try {
      if (activeAssignmentId) {
        await window.api.put(`/api/assignments/${activeAssignmentId}`, payload);
        window.common.showToast("Assignment updated successfully!", "success");
      } else {
        await window.api.post(`/api/courses/${selectedCourse}/assignments`, payload);
        window.common.showToast("Assignment created successfully!", "success");
      }

      window.common.closeModal("assignment-modal");
      await loadData();
    } catch (err) {
      window.common.showToast(err.message || "Failed to save assignment", "error");
    } finally {
      btn.disabled = false;
    }
  });
}
