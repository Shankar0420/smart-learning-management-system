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

    renderCourseDetails(course);
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(wrapper, error.message, () => loadCourseDetails());
  }
}

function renderCourseDetails(course) {
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
