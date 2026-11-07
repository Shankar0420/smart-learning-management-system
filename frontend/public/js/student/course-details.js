/**
 * Smart Learning Management System
 * Course Details Controller
 * Connects to GET /api/courses/{id} and POST /api/courses/{id}/enroll
 */

let currentCourseId = null;
let isEnrolled = false;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "courses");

  const params = new URLSearchParams(window.location.search);
  currentCourseId = params.get("id");

  if (!currentCourseId) {
    window.location.href = "/student/courses.html";
    return;
  }

  await loadCourseDetails();
});

async function loadCourseDetails() {
  const wrapper = document.getElementById("course-details-wrapper");
  const errorArea = document.getElementById("details-error-area");
  errorArea.classList.add("hidden");

  try {
    // Check enrollment and fetch course details
    const [course, enrolledList] = await Promise.all([
      window.api.get(`/api/courses/${currentCourseId}`),
      window.api.get("/api/student/courses").catch(() => [])
    ]);

    const enrolledArr = Array.isArray(enrolledList) ? enrolledList : (enrolledList.courses || []);
    isEnrolled = enrolledArr.some(c => String(c.id || c.courseId || c.course?.id) === String(currentCourseId));

    let materials = [];
    if (isEnrolled) {
      materials = await window.api.get(`/api/courses/${currentCourseId}/materials`).catch(() => []);
    }

    renderCourseDetails(course, Array.isArray(materials) ? materials : []);
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(wrapper, error.message, () => loadCourseDetails());
  }
}

function renderCourseDetails(course, materials = []) {
  const wrapper = document.getElementById("course-details-wrapper");
  const title = course.title || "Course Details";
  const description = course.description || "No course description provided.";
  const teacherName = course.teacherName || course.teacher?.name || "Faculty Member";
  const modules = course.modules || [];

  let totalLessons = 0;
  modules.forEach(m => {
    if (m.lessons) totalLessons += m.lessons.length;
  });

  wrapper.innerHTML = `
    <!-- Hero Card -->
    <div class="card" style="margin-bottom: 2rem;">
      <div class="card-body">
        <div class="flex items-center justify-between flex-wrap gap-4" style="margin-bottom: 1rem;">
          <span class="badge badge-info">Computer Science Department</span>
          ${isEnrolled ? '<span class="badge badge-success">✓ You are Enrolled</span>' : '<span class="badge badge-neutral">Not Enrolled</span>'}
        </div>

        <h1 style="font-size: 2rem; font-weight: 700; color: var(--dark); margin-bottom: 0.75rem;">
          ${window.common.escapeHTML(title)}
        </h1>
        <p style="font-size: 1rem; color: var(--text-secondary); line-height: 1.6; margin-bottom: 1.5rem;">
          ${window.common.escapeHTML(description)}
        </p>

        <div class="flex items-center gap-6 text-muted" style="font-size: 0.875rem; margin-bottom: 1.5rem; flex-wrap: wrap;">
          <span>👨‍🏫 Instructor: <strong>${window.common.escapeHTML(teacherName)}</strong></span>
          <span>📁 Modules: <strong>${modules.length}</strong></span>
          <span>📖 Lessons: <strong>${totalLessons}</strong></span>
          ${isEnrolled ? `<span>📑 Materials: <strong>${materials.length}</strong></span>` : ''}
        </div>

        <div>
          ${isEnrolled ? `
            <a href="/student/learning.html?courseId=${currentCourseId}" class="btn btn-primary btn-lg">
              Continue Learning →
            </a>
          ` : `
            <button id="enroll-btn" class="btn btn-primary btn-lg">
              Enroll in this Course
            </button>
          `}
        </div>
      </div>
    </div>

    ${isEnrolled ? `
      <!-- Study Materials Card -->
      <div class="card" style="margin-bottom: 2rem;">
        <div class="card-header">
          <div>
            <h3 class="card-title">Course Study Materials</h3>
            <p class="text-muted" style="font-size: 0.825rem; margin: 0.25rem 0 0 0;">Download presentation decks (PPT, PPTX) and reference guides (PDF) uploaded by your instructor.</p>
          </div>
          <span class="badge badge-info">${materials.length} files available</span>
        </div>
        <div class="card-body" style="padding: 0;">
          ${materials.length === 0 ? `
            <div style="padding: 2rem; text-align: center;">
              <p class="text-muted" style="margin: 0;">No study materials uploaded for this course yet.</p>
            </div>
          ` : `
            <div class="table-responsive">
              <table class="table">
                <thead>
                  <tr>
                    <th style="width: 80px;">Format</th>
                    <th>Material Title & Details</th>
                    <th>File Name</th>
                    <th>Size</th>
                    <th>Uploaded</th>
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
                        <td>
                          <span class="badge ${badgeClass}" style="font-weight: 700;">${fileType || "FILE"}</span>
                        </td>
                        <td>
                          <strong>${window.common.escapeHTML(m.title)}</strong>
                          ${m.description ? `<p class="text-muted" style="font-size: 0.8rem; margin: 0.2rem 0 0 0;">${window.common.escapeHTML(m.description)}</p>` : ''}
                        </td>
                        <td>
                          <code style="font-size: 0.75rem; background: rgba(37,99,235,0.08); color: var(--primary); padding: 0.1rem 0.35rem; border-radius: 4px;">
                            ${window.common.escapeHTML(m.originalFileName || "material")}
                          </code>
                        </td>
                        <td class="text-muted tabular-nums">${sizeStr}</td>
                        <td class="text-muted tabular-nums">${window.common.formatDate(m.createdAt)}</td>
                        <td style="text-align: right;">
                          <button class="btn btn-secondary btn-sm" onclick="handleDownloadMaterial('${m.id}', '${window.common.escapeHTML(m.originalFileName || 'material')}')">
                            ⬇ Download
                          </button>
                        </td>
                      </tr>
                    `;
                  }).join("")}
                </tbody>
              </table>
            </div>
          `}
        </div>
      </div>
    ` : ''}

    <!-- Modules Syllabus Tree -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">Curriculum Modules & Lessons</h3>
      </div>
      <div class="card-body">
        ${modules.length === 0 ? `
          <div class="empty-state">
            <p class="text-muted">No modules have been published for this course yet.</p>
          </div>
        ` : `
          <div style="display: flex; flex-direction: column; gap: 1rem;">
            ${modules.map((mod, idx) => `
              <div class="module-accordion">
                <div class="module-header">
                  <span><strong>Module ${idx + 1}:</strong> ${window.common.escapeHTML(mod.title || "Untitled Module")}</span>
                  <span class="badge badge-neutral">${(mod.lessons || []).length} lessons</span>
                </div>
                <div>
                  ${(mod.lessons || []).map((les, lIdx) => `
                    <div class="lesson-item">
                      <span>Lesson ${idx + 1}.${lIdx + 1}: ${window.common.escapeHTML(les.title || "Lesson")}</span>
                      ${isEnrolled ? `
                        <a href="/student/learning.html?courseId=${currentCourseId}&lessonId=${les.id}" class="text-primary" style="font-size: 0.8rem;">
                          Study →
                        </a>
                      ` : `
                        <span class="text-muted" style="font-size: 0.75rem;">Enroll to view</span>
                      `}
                    </div>
                  `).join("")}
                </div>
              </div>
            `).join("")}
          </div>
        `}
      </div>
    </div>
  `;

  document.getElementById("enroll-btn")?.addEventListener("click", async () => {
    const btn = document.getElementById("enroll-btn");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Enrolling...';

    try {
      await window.api.post(`/api/courses/${currentCourseId}/enroll`, {});
      window.common.showToast("Successfully enrolled!", "success");
      isEnrolled = true;
      await loadCourseDetails();
    } catch (err) {
      window.common.showToast(err.message || "Enrollment failed", "error");
      btn.disabled = false;
      btn.innerText = "Enroll in this Course";
    }
  });
}

window.handleDownloadMaterial = async function(materialId, filename) {
  try {
    window.common.showToast("Downloading material...", "info");
    await window.api.download(`/api/materials/${materialId}/download`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download material", "error");
  }
};
