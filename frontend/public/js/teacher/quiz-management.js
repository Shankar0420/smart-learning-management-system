/**
 * Smart Learning Management System
 * Teacher Quiz Management Controller
 * Connects to:
 * POST /api/courses/{id}/quizzes
 * PUT /api/quizzes/{id}
 * DELETE /api/quizzes/{id}
 * POST /api/quizzes/{id}/questions
 */

let courseId = null;
let currentQuizzes = [];
let activeQuizId = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "courses");

  const params = new URLSearchParams(window.location.search);
  courseId = params.get("courseId") || params.get("id");

  if (!courseId) {
    window.location.href = "/teacher/courses.html";
    return;
  }

  document.getElementById("back-to-curriculum").href = `/teacher/course-details.html?id=${courseId}`;

  setupForms();
  await loadQuizzes();
});

async function loadQuizzes() {
  const container = document.getElementById("quiz-list-container");
  const errorArea = document.getElementById("quiz-mgmt-error");
  errorArea.classList.add("hidden");

  try {
    const course = await window.api.get(`/api/courses/${courseId}`);
    document.getElementById("quiz-page-course-title").innerText = `Quizzes · ${course.title || "Course"}`;

    currentQuizzes = course.quizzes || [];

    if (currentQuizzes.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Quizzes Created",
        "Build interactive multiple-choice assessments for your students with automated MariaDB scoring.",
        '<button class="btn btn-primary btn-sm" onclick="openCreateQuizModal()">+ Create First Quiz</button>'
      );
      return;
    }

    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 1.5rem;">
        ${currentQuizzes.map(quiz => {
          const questions = quiz.questions || [];
          return `
            <div class="card">
              <div class="card-header" style="background-color: var(--bg-alt);">
                <div>
                  <h3 class="card-title">${window.common.escapeHTML(quiz.title || "Quiz")}</h3>
                  <span class="text-muted" style="font-size: 0.8rem;">${questions.length} Question${questions.length === 1 ? '' : 's'}</span>
                </div>

                <div class="flex items-center gap-2">
                  <button class="btn btn-primary btn-sm" onclick="openAddQuestionModal('${quiz.id}')">
                    + Add Question
                  </button>
                  <button class="btn btn-secondary btn-sm" onclick="openEditQuizModal('${quiz.id}', '${window.common.escapeHTML(quiz.title)}')">
                    Rename
                  </button>
                  <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteQuiz('${quiz.id}', '${window.common.escapeHTML(quiz.title)}')">
                    Delete Quiz
                  </button>
                </div>
              </div>

              <div class="card-body">
                ${questions.length === 0 ? `
                  <p class="text-muted" style="font-size: 0.875rem;">No questions added yet. Click "+ Add Question" to define MCQs and correct options.</p>
                ` : `
                  <div style="display: flex; flex-direction: column; gap: 1rem;">
                    ${questions.map((q, idx) => `
                      <div style="border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 1rem;">
                        <div class="flex items-center justify-between" style="margin-bottom: 0.5rem;">
                          <strong style="color: var(--dark); font-size: 0.95rem;">Q${idx + 1}. ${window.common.escapeHTML(q.questionText || q.text)}</strong>
                          <span class="badge badge-neutral">${q.marks || 1} mark${(q.marks || 1) > 1 ? 's' : ''}</span>
                        </div>

                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.5rem; margin-top: 0.5rem;">
                          ${(q.options || []).map(opt => `
                            <div style="padding: 0.4rem 0.75rem; border-radius: 4px; font-size: 0.85rem; background: ${opt.correct || opt.isCorrect ? 'var(--success-bg)' : 'var(--bg-alt)'}; border: 1px solid ${opt.correct || opt.isCorrect ? 'var(--success-border)' : 'var(--border-color)'};">
                              ${opt.correct || opt.isCorrect ? '<strong class="text-success">✓ Correct: </strong>' : '○ '}
                              ${window.common.escapeHTML(opt.optionText || opt.text)}
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
        }).join("")}
      </div>
    `;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadQuizzes());
  }
}

// Modal Handlers
window.openCreateQuizModal = function() {
  activeQuizId = null;
  document.getElementById("quiz-modal-title").innerText = "Create Assessment Quiz";
  document.getElementById("quiz-title-input").value = "";
  window.common.openModal("quiz-modal");
};

window.openEditQuizModal = function(id, title) {
  activeQuizId = id;
  document.getElementById("quiz-modal-title").innerText = "Edit Quiz Assessment";
  document.getElementById("quiz-title-input").value = title || "";
  window.common.openModal("quiz-modal");
};

window.handleDeleteQuiz = function(id, title) {
  window.common.confirmAction(`Are you sure you want to delete quiz "${title}"?`, async () => {
    try {
      await window.api.delete(`/api/quizzes/${id}`);
      window.common.showToast("Quiz deleted successfully!", "success");
      await loadQuizzes();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete quiz", "error");
    }
  });
};

window.openAddQuestionModal = function(quizId) {
  activeQuizId = quizId;
  document.getElementById("q-text-input").value = "";
  document.getElementById("q-marks-input").value = "1";
  document.querySelectorAll(".q-opt-input").forEach(input => input.value = "");
  document.querySelector('input[name="correct-option"][value="0"]').checked = true;
  window.common.openModal("question-modal");
};

function setupForms() {
  // Quiz Form
  document.getElementById("quiz-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("quiz-title-input").value.trim();
    if (!title) return;

    const btn = document.getElementById("save-quiz-btn");
    btn.disabled = true;

    try {
      if (activeQuizId) {
        await window.api.put(`/api/quizzes/${activeQuizId}`, { title });
        window.common.showToast("Quiz updated successfully!", "success");
      } else {
        await window.api.post(`/api/courses/${courseId}/quizzes`, { title });
        window.common.showToast("Quiz created successfully!", "success");
      }
      window.common.closeModal("quiz-modal");
      await loadQuizzes();
    } catch (err) {
      window.common.showToast(err.message || "Failed to save quiz", "error");
    } finally {
      btn.disabled = false;
    }
  });

  // Question Form
  document.getElementById("question-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const questionText = document.getElementById("q-text-input").value.trim();
    const marks = Number(document.getElementById("q-marks-input").value) || 1;
    const optionInputs = document.querySelectorAll(".q-opt-input");
    const correctIdx = Number(document.querySelector('input[name="correct-option"]:checked')?.value || 0);

    const options = [];
    optionInputs.forEach((inp, idx) => {
      const text = inp.value.trim();
      if (text) {
        options.push({
          optionText: text,
          text: text,
          isCorrect: idx === correctIdx,
          correct: idx === correctIdx
        });
      }
    });

    if (!questionText || options.length < 2) {
      window.common.showToast("Question text and at least 2 options are required.", "error");
      return;
    }

    const btn = document.getElementById("save-question-btn");
    btn.disabled = true;

    try {
      await window.api.post(`/api/quizzes/${activeQuizId}/questions`, {
        questionText,
        text: questionText,
        marks,
        options,
        correctOptionIndex: correctIdx
      });

      window.common.showToast("Question added to quiz successfully!", "success");
      window.common.closeModal("question-modal");
      await loadQuizzes();
    } catch (err) {
      window.common.showToast(err.message || "Failed to add question", "error");
    } finally {
      btn.disabled = false;
    }
  });
}
