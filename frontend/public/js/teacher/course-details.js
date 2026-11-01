/**
 * Smart Learning Management System
 * Teacher Curriculum (Modules & Lessons) Controller
 * Connects to:
 * POST /api/courses/{id}/modules
 * PUT /api/modules/{id}
 * DELETE /api/modules/{id}
 * POST /api/modules/{id}/lessons
 * PUT /api/lessons/{id}
 * DELETE /api/lessons/{id}
 */

let courseId = null;
let currentCourse = null;
let activeModuleId = null;
let activeLessonId = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "courses");

  const params = new URLSearchParams(window.location.search);
  courseId = params.get("id");

  if (!courseId) {
    window.location.href = "/teacher/courses.html";
    return;
  }

  // Set link targets
  document.getElementById("btn-manage-quizzes").href = `/teacher/quiz-management.html?courseId=${courseId}`;
  document.getElementById("btn-manage-assignments").href = `/teacher/assignment-management.html?courseId=${courseId}`;

  setupModalForms();
  await loadCurriculum();
});

async function loadCurriculum() {
  const container = document.getElementById("curriculum-container");
  const countBadge = document.getElementById("modules-count-badge");
  const errorArea = document.getElementById("curriculum-error-area");
  errorArea.classList.add("hidden");

  try {
    currentCourse = await window.api.get(`/api/courses/${courseId}`);
    document.getElementById("teacher-course-title").innerText = currentCourse.title || "Curriculum Overview";

    const modules = currentCourse.modules || [];
    countBadge.innerText = `${modules.length} Module${modules.length === 1 ? '' : 's'}`;

    if (modules.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Modules Yet",
        "This course does not have any modules created. Add a module to start organizing lessons.",
        '<button class="btn btn-primary btn-sm" onclick="openModuleModal()">+ Add First Module</button>'
      );
      return;
    }

    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 1.25rem;">
        ${modules.map((mod, modIdx) => `
          <div class="module-accordion" style="border: 1px solid var(--border-color); border-radius: var(--radius-md);">
            <!-- Module Bar -->
            <div class="module-header" style="background-color: var(--bg-alt); padding: 0.875rem 1.25rem; display: flex; justify-content: space-between; align-items: center;">
              <div>
                <span style="font-weight: 700; color: var(--dark); font-size: 0.95rem;">
                  Module ${modIdx + 1}: ${window.common.escapeHTML(mod.title || "Untitled")}
                </span>
                <span class="badge badge-neutral" style="margin-left: 0.5rem;">${(mod.lessons || []).length} lessons</span>
              </div>

              <div class="flex items-center gap-2">
                <button class="btn btn-primary btn-sm" onclick="openLessonModal('${mod.id}')">
                  + Add Lesson
                </button>
                <button class="btn btn-secondary btn-sm" onclick="openModuleModal('${mod.id}', '${window.common.escapeHTML(mod.title)}')">
                  Edit
                </button>
                <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteModule('${mod.id}', '${window.common.escapeHTML(mod.title)}')">
                  Delete
                </button>
              </div>
            </div>

            <!-- Lessons List Inside Module -->
            <div style="padding: 0.5rem 1rem;">
              ${(mod.lessons || []).length === 0 ? `
                <p class="text-muted" style="padding: 1rem; font-size: 0.85rem; text-align: center;">No lessons in this module yet.</p>
              ` : `
                <div style="display: flex; flex-direction: column;">
                  ${(mod.lessons || []).map((les, lesIdx) => `
                    <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 0.5rem; border-bottom: 1px solid var(--border-light);">
                      <div>
                        <strong>${modIdx + 1}.${lesIdx + 1}</strong>
                        <span style="margin-left: 0.5rem; color: var(--dark); font-weight: 500;">
                          ${window.common.escapeHTML(les.title || "Lesson")}
                        </span>
                        <div class="text-muted" style="font-size: 0.75rem; margin-top: 0.2rem; max-width: 500px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                          ${window.common.escapeHTML(les.content ? les.content.substring(0, 100) : "No text content")}...
                        </div>
                      </div>

                      <div class="flex items-center gap-2">
                        <button class="btn btn-secondary btn-sm" onclick='openEditLessonModal(${JSON.stringify(les)})'>
                          Edit
                        </button>
                        <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteLesson('${les.id}', '${window.common.escapeHTML(les.title)}')">
                          Delete
                        </button>
                      </div>
                    </div>
                  `).join("")}
                </div>
              `}
            </div>
          </div>
        `).join("")}
      </div>
    `;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadCurriculum());
  }
}

// Module Modal Helpers
window.openModuleModal = function(moduleId = null, title = "") {
  activeModuleId = moduleId;
  document.getElementById("module-modal-title").innerText = moduleId ? "Edit Module" : "Add Course Module";
  document.getElementById("module-title-input").value = title || "";
  window.common.openModal("module-modal");
};

window.handleDeleteModule = function(moduleId, title) {
  window.common.confirmAction(`Are you sure you want to delete module "${title}" and all its lessons?`, async () => {
    try {
      await window.api.delete(`/api/modules/${moduleId}`);
      window.common.showToast("Module deleted successfully!", "success");
      await loadCurriculum();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete module", "error");
    }
  });
};

// Lesson Modal Helpers
window.openLessonModal = function(moduleId) {
  activeModuleId = moduleId;
  activeLessonId = null;
  document.getElementById("lesson-modal-title").innerText = "Add Lesson to Module";
  document.getElementById("lesson-title-input").value = "";
  document.getElementById("lesson-content-input").value = "";
  window.common.openModal("lesson-modal");
};

window.openEditLessonModal = function(lesson) {
  activeLessonId = lesson.id;
  document.getElementById("lesson-modal-title").innerText = "Edit Lesson";
  document.getElementById("lesson-title-input").value = lesson.title || "";
  document.getElementById("lesson-content-input").value = lesson.content || "";
  window.common.openModal("lesson-modal");
};

window.handleDeleteLesson = function(lessonId, title) {
  window.common.confirmAction(`Are you sure you want to delete lesson "${title}"?`, async () => {
    try {
      await window.api.delete(`/api/lessons/${lessonId}`);
      window.common.showToast("Lesson deleted successfully!", "success");
      await loadCurriculum();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete lesson", "error");
    }
  });
};

function setupModalForms() {
  // Module Form
  document.getElementById("module-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("module-title-input").value.trim();
    if (!title) return;

    const saveBtn = document.getElementById("save-module-btn");
    saveBtn.disabled = true;

    try {
      if (activeModuleId) {
        await window.api.put(`/api/modules/${activeModuleId}`, { title });
        window.common.showToast("Module updated successfully!", "success");
      } else {
        await window.api.post(`/api/courses/${courseId}/modules`, { title });
        window.common.showToast("Module added successfully!", "success");
      }

      window.common.closeModal("module-modal");
      await loadCurriculum();
    } catch (err) {
      window.common.showToast(err.message || "Failed to save module", "error");
    } finally {
      saveBtn.disabled = false;
    }
  });

  // Lesson Form
  document.getElementById("lesson-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("lesson-title-input").value.trim();
    const content = document.getElementById("lesson-content-input").value.trim();

    if (!title || !content) {
      window.common.showToast("Title and content are required.", "error");
      return;
    }

    const saveBtn = document.getElementById("save-lesson-btn");
    saveBtn.disabled = true;

    try {
      if (activeLessonId) {
        await window.api.put(`/api/lessons/${activeLessonId}`, { title, content });
        window.common.showToast("Lesson updated successfully!", "success");
      } else {
        await window.api.post(`/api/modules/${activeModuleId}/lessons`, { title, content });
        window.common.showToast("Lesson added successfully!", "success");
      }

      window.common.closeModal("lesson-modal");
      await loadCurriculum();
    } catch (err) {
      window.common.showToast(err.message || "Failed to save lesson", "error");
    } finally {
      saveBtn.disabled = false;
    }
  });
}
