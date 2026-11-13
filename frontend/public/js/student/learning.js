/**
 * Smart Learning Management System
 * Student Learning & Lesson Completion Controller
 * Connects to GET /api/courses/{id}, GET /api/courses/{id}/progress, and POST /api/lessons/{id}/complete
 */

let courseId = null;
let currentLessonId = null;
let courseData = null;
let progressData = null;
let materialsData = [];
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
    const [course, progress, materials] = await Promise.all([
      window.api.get(`/api/courses/${courseId}`),
      window.api.get(`/api/courses/${courseId}/progress`).catch(err => {
        console.warn("Could not load course progress:", err);
        return { completedLessons: 0, totalLessons: 0, percentage: 0 };
      }),
      window.api.get(`/api/courses/${courseId}/materials`).catch(() => [])
    ]);

    courseData = course;
    progressData = progress;
    materialsData = Array.isArray(materials) ? materials : [];

    document.getElementById("learning-course-title").innerText = course.title || "Course Classroom";

    updateProgressBar(progress);
    renderModulesTree();
    renderMaterialsList();
    await loadDiscussions();

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

function renderMaterialsList() {
  const container = document.getElementById("learning-materials-list");
  if (!container) return;

  if (materialsData.length === 0) {
    container.innerHTML = `<p class="text-muted" style="padding: 0.5rem; text-align: center; font-size: 0.8rem; margin: 0;">No study materials uploaded for this course yet.</p>`;
    return;
  }

  container.innerHTML = materialsData.map(m => {
    const fileType = (m.fileType || "").toUpperCase();
    let badgeClass = "badge-info";
    if (fileType === "PDF") badgeClass = "badge-danger";
    else if (fileType === "PPT" || fileType === "PPTX") badgeClass = "badge-warning";

    const sizeKB = Math.round((m.fileSize || 0) / 1024);
    const sizeStr = sizeKB > 1024 ? (sizeKB / 1024).toFixed(1) + " MB" : sizeKB + " KB";

    return `
      <div style="padding: 0.5rem; border-bottom: 1px solid var(--border-color); display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
        <div style="min-width: 0; flex: 1;">
          <div style="display: flex; align-items: center; gap: 0.35rem; margin-bottom: 0.15rem;">
            <span class="badge ${badgeClass}" style="font-size: 0.65rem; padding: 0.1rem 0.3rem;">${fileType || "DOC"}</span>
            <span style="font-size: 0.825rem; font-weight: 600; color: var(--dark); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block;" title="${window.common.escapeHTML(m.title)}">
              ${window.common.escapeHTML(m.title)}
            </span>
          </div>
          <span class="text-muted" style="font-size: 0.7rem;">${sizeStr}</span>
        </div>
        <button class="btn btn-secondary btn-sm" style="padding: 0.25rem 0.5rem; font-size: 0.75rem;" title="Download material" onclick="handleDownloadMaterial('${m.id}', '${window.common.escapeHTML(m.originalFileName || 'material')}')">
          ⬇
        </button>
      </div>
    `;
  }).join("");
}

window.handleDownloadMaterial = async function(materialId, filename) {
  try {
    window.common.showToast("Downloading material...", "info");
    await window.api.download(`/api/materials/${materialId}/download`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download material", "error");
  }
};

/**
 * Course Discussion Forum Controller
 */
let currentThreadId = null;

async function loadDiscussions() {
  const container = document.getElementById("discussions-container");
  if (!container) return;

  try {
    const threads = await window.api.get(`/api/discussions/course/${courseId}`);
    const list = Array.isArray(threads) ? threads : [];

    if (list.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 1.5rem; color: #64748b; font-size: 0.85rem;">
          No discussions started for this course yet. Be the first to ask a question!
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(t => `
      <div class="forum-thread-card ${t.pinned ? 'pinned' : ''}" onclick="window.openViewThreadModal(${t.id})">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.35rem;">
          <div style="display: flex; gap: 0.4rem; align-items: center;">
            ${t.pinned ? '<span class="badge badge-warning" style="font-size: 0.7rem;">📌 PINNED</span>' : ''}
            <h4 style="font-size: 1rem; font-weight: 700; color: #0f172a; margin: 0;">${window.common.escapeHTML(t.title)}</h4>
          </div>
          <span class="badge badge-neutral" style="font-size: 0.72rem;">💬 ${t.repliesCount || 0} replies</span>
        </div>
        <p style="font-size: 0.85rem; color: #475569; margin-bottom: 0.5rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
          ${window.common.escapeHTML(t.content)}
        </p>
        <div style="font-size: 0.75rem; color: #94a3b8; display: flex; gap: 1rem;">
          <span>👤 ${window.common.escapeHTML(t.userName || "Student")}</span>
          <span>📅 ${window.common.formatDate(t.createdAt)}</span>
        </div>
      </div>
    `).join("");
  } catch (err) {
    container.innerHTML = `<p class="text-muted" style="font-size: 0.85rem;">Discussions will load once course activity begins.</p>`;
  }
}

window.openNewThreadModal = function() {
  const form = document.getElementById("new-thread-form");
  if (form) form.reset();
  window.common.openModal("new-thread-modal");
};

window.openViewThreadModal = async function(threadId) {
  currentThreadId = threadId;
  const titleEl = document.getElementById("view-thread-title");
  const metaEl = document.getElementById("view-thread-meta");
  const bodyEl = document.getElementById("view-thread-body");
  const repliesContainer = document.getElementById("thread-replies-list");
  const badgeEl = document.getElementById("replies-count-badge");
  const replyInput = document.getElementById("reply-input-text");

  if (replyInput) replyInput.value = "";
  if (titleEl) titleEl.innerText = "Loading discussion...";
  if (bodyEl) bodyEl.innerHTML = `<div class="loading-container"><div class="spinner-primary"></div></div>`;
  if (repliesContainer) repliesContainer.innerHTML = "";

  window.common.openModal("view-thread-modal");

  try {
    const thread = await window.api.get(`/api/discussions/threads/${threadId}`);
    if (!thread) return;

    if (titleEl) titleEl.innerText = thread.title;
    if (metaEl) {
      metaEl.innerText = `Posted by ${thread.userName || "Student"} on ${window.common.formatDate(thread.createdAt)}`;
    }
    if (bodyEl) {
      bodyEl.innerText = thread.content;
    }

    const replies = thread.replies || [];
    if (badgeEl) badgeEl.innerText = replies.length;

    if (replies.length === 0) {
      repliesContainer.innerHTML = `<div style="text-align: center; color: #94a3b8; font-size: 0.85rem; padding: 1rem;">No replies yet. Be the first to answer!</div>`;
    } else {
      repliesContainer.innerHTML = replies.map(r => `
        <div class="reply-card ${r.accepted ? 'accepted' : ''}">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.4rem;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <strong>${window.common.escapeHTML(r.userName || "User")}</strong>
              <span class="badge ${r.userRole === 'TEACHER' ? 'badge-primary' : 'badge-neutral'}" style="font-size: 0.68rem;">${r.userRole || 'STUDENT'}</span>
              ${r.accepted ? '<span class="badge badge-success" style="font-size: 0.7rem;">✓ ACCEPTED ANSWER</span>' : ''}
            </div>
            <span style="font-size: 0.72rem; color: #94a3b8;">${window.common.formatDate(r.createdAt)}</span>
          </div>
          <p style="font-size: 0.88rem; color: #334155; margin: 0; line-height: 1.5;">${window.common.escapeHTML(r.content)}</p>
          ${!r.accepted ? `
            <div style="display: flex; justify-content: flex-end; margin-top: 0.35rem;">
              <button class="btn btn-secondary btn-sm" style="font-size: 0.72rem; padding: 0.15rem 0.5rem;" onclick="window.acceptReply(${r.id})">✓ Accept Answer</button>
            </div>
          ` : ''}
        </div>
      `).join("");
    }
  } catch (err) {
    window.common.showToast(err.message || "Failed to load thread details", "error");
  }
};

window.submitReply = async function() {
  const replyInput = document.getElementById("reply-input-text");
  const content = replyInput?.value.trim();
  if (!content) {
    window.common.showToast("Please enter your answer text", "warning");
    return;
  }

  const btn = document.getElementById("btn-submit-reply");
  if (btn) btn.disabled = true;

  try {
    await window.api.post(`/api/discussions/threads/${currentThreadId}/replies`, { content });
    window.common.showToast("Reply published!", "success");
    if (replyInput) replyInput.value = "";
    await openViewThreadModal(currentThreadId);
    await loadDiscussions();
  } catch (err) {
    window.common.showToast(err.message || "Failed to post reply", "error");
  } finally {
    if (btn) btn.disabled = false;
  }
};

window.acceptReply = async function(replyId) {
  try {
    await window.api.put(`/api/discussions/replies/${replyId}/accept`, {});
    window.common.showToast("Marked as accepted answer!", "success");
    await openViewThreadModal(currentThreadId);
  } catch (err) {
    window.common.showToast(err.message || "Failed to accept reply", "error");
  }
};

// Form listener for new thread
document.addEventListener("DOMContentLoaded", () => {
  const threadForm = document.getElementById("new-thread-form");
  if (threadForm) {
    threadForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const title = document.getElementById("thread-title-input")?.value.trim();
      const content = document.getElementById("thread-content-input")?.value.trim();
      if (!title || !content) return;

      const btn = document.getElementById("submit-thread-btn");
      if (btn) btn.disabled = true;

      try {
        await window.api.post("/api/discussions/threads", {
          courseId: Number(courseId),
          title,
          content
        });

        window.common.showToast("Question posted to course forum!", "success");
        window.common.closeModal("new-thread-modal");
        threadForm.reset();
        await loadDiscussions();
      } catch (err) {
        window.common.showToast(err.message || "Failed to post question", "error");
      } finally {
        if (btn) btn.disabled = false;
      }
    });
  }
});
