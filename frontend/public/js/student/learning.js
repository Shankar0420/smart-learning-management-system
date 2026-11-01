/**
 * Smart Learning Management System
 * Student Learning & Lesson Completion Controller
 * Connects to GET /api/courses/{id}, GET /api/courses/{id}/progress, and POST /api/lessons/{id}/complete
 */

let courseId = null;
let currentLessonId = null;
let courseData = null;
let progressData = null;
let completedLessonIds = new Set();

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "courses");

  const params = new URLSearchParams(window.location.search);
  courseId = params.get("courseId");
  currentLessonId = params.get("lessonId");

  if (!courseId) {
    window.location.href = "/student/courses.html";
    return;
  }

  // Setup Quiz and Assignment buttons if courseId available
  const quizLink = document.getElementById("course-quiz-link");
  if (quizLink) {
    quizLink.href = `/student/quiz.html?courseId=${courseId}`;
    quizLink.classList.remove("hidden");
  }

  const assignmentLink = document.getElementById("course-assignment-link");
  if (assignmentLink) {
    assignmentLink.href = `/student/assignments.html?courseId=${courseId}`;
    assignmentLink.classList.remove("hidden");
  }

  await loadLearningContent();
});

async function loadLearningContent() {
  try {
    const [course, progress] = await Promise.all([
      window.api.get(`/api/courses/${courseId}`),
      window.api.get(`/api/courses/${courseId}/progress`).catch(err => {
        console.warn("Could not load course progress:", err);
        return { completedLessons: 0, totalLessons: 0, percentage: 0 };
      })
    ]);

    courseData = course;
    progressData = progress;

    document.getElementById("learning-course-title").innerText = course.title || "Course Classroom";

    updateProgressBar(progress);
    renderModulesTree();

    // Select either the requested lesson or the first available lesson
    const firstLesson = findLesson(currentLessonId) || findFirstLesson();
    if (firstLesson) {
      selectLesson(firstLesson);
    }
  } catch (error) {
    window.common.showToast(error.message, "error");
    document.getElementById("learning-modules-tree").innerHTML = `
      <div class="empty-state">
        <p class="text-danger">${window.common.escapeHTML(error.message)}</p>
      </div>
    `;
  }
}

function updateProgressBar(progress) {
  const percent = Math.min(100, Math.max(0, progress.percentage ?? progress.progress ?? 0));
  const completed = progress.completedLessons ?? 0;
  const total = progress.totalLessons ?? 0;

  const fill = document.getElementById("learning-progress-fill");
  const label = document.getElementById("progress-percentage-label");

  if (fill) {
    fill.style.width = `${percent}%`;
    if (percent === 100) fill.classList.add("success");
  }

  if (label) {
    label.innerText = `${percent}% Completed (${completed} of ${total} lessons)`;
  }
}

function renderModulesTree() {
  const container = document.getElementById("learning-modules-tree");
  const modules = courseData.modules || [];

  if (modules.length === 0) {
    container.innerHTML = `<p class="text-muted" style="padding: 1rem; text-align: center;">No modules available.</p>`;
    return;
  }

  container.innerHTML = modules.map((mod, modIdx) => `
    <div class="module-accordion" style="margin-bottom: 0.5rem;">
      <div class="module-header" style="font-size: 0.825rem; padding: 0.5rem 0.75rem;">
        <span><strong>M${modIdx + 1}:</strong> ${window.common.escapeHTML(mod.title || "Module")}</span>
      </div>
      <div>
        ${(mod.lessons || []).map(les => {
          const isSelected = String(les.id) === String(currentLessonId);
          const isDone = les.completed || completedLessonIds.has(String(les.id));
          if (les.completed) completedLessonIds.add(String(les.id));

          return `
            <div
              class="lesson-item ${isSelected ? 'active' : ''}"
              id="lesson-item-${les.id}"
              onclick="handleSelectLesson('${les.id}')"
            >
              <span>${window.common.escapeHTML(les.title || "Lesson")}</span>
              <span class="lesson-status-icon ${isDone ? 'completed' : ''}" id="icon-lesson-${les.id}">
                ${isDone ? '✓' : '○'}
              </span>
            </div>
          `;
        }).join("")}
      </div>
    </div>
  `).join("");
}

function findLesson(id) {
  if (!id || !courseData?.modules) return null;
  for (const mod of courseData.modules) {
    for (const les of mod.lessons || []) {
      if (String(les.id) === String(id)) return les;
    }
  }
  return null;
}

function findFirstLesson() {
  if (!courseData?.modules) return null;
  for (const mod of courseData.modules) {
    if (mod.lessons && mod.lessons.length > 0) return mod.lessons[0];
  }
  return null;
}

window.handleSelectLesson = function(lesId) {
  const lesson = findLesson(lesId);
  if (lesson) selectLesson(lesson);
};

function selectLesson(lesson) {
  currentLessonId = lesson.id;

  // Highlight in tree
  document.querySelectorAll(".lesson-item").forEach(el => el.classList.remove("active"));
  document.getElementById(`lesson-item-${lesson.id}`)?.classList.add("active");

  const viewer = document.getElementById("lesson-viewer-container");
  const isDone = lesson.completed || completedLessonIds.has(String(lesson.id));

  viewer.innerHTML = `
    <div class="flex items-center justify-between flex-wrap gap-4" style="margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border-color);">
      <div>
        <span class="badge badge-info" style="margin-bottom: 0.5rem;">Interactive Lesson</span>
        <h2 class="lesson-viewer-title" style="margin-bottom: 0;">${window.common.escapeHTML(lesson.title || "Lesson")}</h2>
      </div>

      <div>
        ${isDone ? `
          <span class="badge badge-success" style="font-size: 0.85rem; padding: 0.5rem 1rem;">
            ✓ Completed
          </span>
        ` : `
          <button id="mark-complete-btn" class="btn btn-primary" onclick="markLessonComplete('${lesson.id}', this)">
            Mark as Completed
          </button>
        `}
      </div>
    </div>

    <div class="lesson-viewer-body">
      ${lesson.content ? window.common.escapeHTML(lesson.content) : "<em>This lesson has no written text content yet.</em>"}
    </div>
  `;
}

window.markLessonComplete = async function(lessonId, buttonEl) {
  buttonEl.disabled = true;
  buttonEl.innerHTML = '<span class="spinner"></span> Updating...';

  try {
    await window.api.post(`/api/lessons/${lessonId}/complete`, {});
    window.common.showToast("Lesson marked as completed!", "success");

    completedLessonIds.add(String(lessonId));

    // Refresh progress from backend
    const updatedProgress = await window.api.get(`/api/courses/${courseId}/progress`).catch(() => null);
    if (updatedProgress) updateProgressBar(updatedProgress);

    // Refresh icon in tree
    const icon = document.getElementById(`icon-lesson-${lessonId}`);
    if (icon) {
      icon.innerText = "✓";
      icon.className = "lesson-status-icon completed";
    }

    // Refresh view
    const currentLesson = findLesson(lessonId);
    if (currentLesson) {
      currentLesson.completed = true;
      selectLesson(currentLesson);
    }
  } catch (error) {
    window.common.showToast(error.message || "Failed to mark lesson complete", "error");
    buttonEl.disabled = false;
    buttonEl.innerText = "Mark as Completed";
  }
};
