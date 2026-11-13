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
                  <div class="flex items-center gap-2">
                    <h3 class="card-title">${window.common.escapeHTML(quiz.title || "Quiz")}</h3>
                    <span class="badge ${quiz.active ? 'badge-success' : 'badge-neutral'}">${quiz.active ? 'Published' : 'Draft'}</span>
                    ${quiz.timeLimitMinutes ? `<span class="badge badge-info" style="font-size: 0.72rem;">⏱️ ${quiz.timeLimitMinutes} mins</span>` : ''}
                    ${quiz.negativeMarking ? `<span class="badge badge-warning" style="font-size: 0.72rem;">⚠️ Neg (-0.25)</span>` : ''}
                    ${quiz.semester ? `<span class="badge" style="background:#e0e7ff; color:#3730a3; font-size: 0.72rem;">Sem ${quiz.semester}</span>` : ''}
                  </div>
                  <span class="text-muted" style="font-size: 0.8rem;">${questions.length} Question${questions.length === 1 ? '' : 's'} · Total Marks: ${quiz.totalMarks || 0}</span>
                </div>

                <div class="flex items-center gap-2">
                  <button class="btn ${quiz.active ? 'btn-secondary' : 'btn-primary'} btn-sm" onclick="handleTogglePublish('${quiz.id}', ${quiz.active})">
                    ${quiz.active ? 'Unpublish' : 'Publish'}
                  </button>
                  <button class="btn btn-outline btn-sm" onclick="openAddQuestionModal('${quiz.id}')">
                    + Add Question
                  </button>
                  <button class="btn btn-secondary btn-sm" onclick="openEditQuizModal('${quiz.id}', '${window.common.escapeHTML(quiz.title)}')">
                    Rename
                  </button>
                  <button class="btn btn-danger-outline btn-sm" onclick="handleDeleteQuiz('${quiz.id}', '${window.common.escapeHTML(quiz.title)}')">
                    Delete
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
                          <div class="flex items-center gap-2">
                            <span class="badge badge-neutral">${q.marks || 1} mark${(q.marks || 1) > 1 ? 's' : ''}</span>
                            <button class="btn btn-danger-outline btn-sm" style="padding: 0.1rem 0.4rem; font-size: 0.75rem;" onclick="handleDeleteQuestion('${q.id}')">✕ Delete</button>
                          </div>
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

window.handleTogglePublish = async function(quizId, currentActive) {
  const action = currentActive ? "unpublish" : "publish";
  window.common.confirmAction(`Are you sure you want to ${action} this quiz? ${action === 'publish' ? 'Enrolled students will immediately be able to attempt it.' : 'Students will no longer see or attempt it.'}`, async () => {
    try {
      await window.api.put(`/api/quizzes/${quizId}/publish`, {});
      window.common.showToast(`Quiz has been ${action}ed successfully!`, "success");
      await loadQuizzes();
    } catch (err) {
      window.common.showToast(err.message || `Failed to ${action} quiz`, "error");
    }
  });
};

window.handleDeleteQuestion = function(questionId) {
  window.common.confirmAction("Are you sure you want to delete this question?", async () => {
    try {
      await window.api.delete(`/api/quizzes/questions/${questionId}`);
      window.common.showToast("Question deleted successfully!", "success");
      await loadQuizzes();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete question", "error");
    }
  });
};

window.handleQuestionTypeChange = function() {
  const qType = document.getElementById("q-type-select")?.value || "MCQ";
  const container = document.getElementById("options-inputs-container");
  if (!container) return;

  if (qType === "TF") {
    container.innerHTML = `
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="0" checked title="Mark True as correct" />
        <input type="text" class="form-input q-opt-input" value="True" readonly style="background: var(--bg-alt); font-weight: 600;" />
      </div>
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="1" title="Mark False as correct" />
        <input type="text" class="form-input q-opt-input" value="False" readonly style="background: var(--bg-alt); font-weight: 600;" />
      </div>
    `;
  } else {
    container.innerHTML = `
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="0" checked title="Mark Option 1 as correct" />
        <input type="text" class="form-input q-opt-input" placeholder="Option 1" required />
      </div>
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="1" title="Mark Option 2 as correct" />
        <input type="text" class="form-input q-opt-input" placeholder="Option 2" required />
      </div>
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="2" title="Mark Option 3 as correct" />
        <input type="text" class="form-input q-opt-input" placeholder="Option 3" required />
      </div>
      <div class="flex items-center gap-2">
        <input type="radio" name="correct-option" value="3" title="Mark Option 4 as correct" />
        <input type="text" class="form-input q-opt-input" placeholder="Option 4" required />
      </div>
    `;
  }
};

window.openAddQuestionModal = function(quizId) {
  activeQuizId = quizId;
  document.getElementById("q-text-input").value = "";
  document.getElementById("q-marks-input").value = "1";
  const qTypeSelect = document.getElementById("q-type-select");
  if (qTypeSelect) {
    qTypeSelect.value = "MCQ";
    handleQuestionTypeChange();
  }
  document.querySelectorAll(".q-opt-input").forEach(input => input.value = "");
  const firstRadio = document.querySelector('input[name="correct-option"][value="0"]');
  if (firstRadio) firstRadio.checked = true;
  window.common.openModal("question-modal");
};

function setupForms() {
  // Quiz Form
  document.getElementById("quiz-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("quiz-title-input").value.trim();
    if (!title) return;

    const timeLimitMinutes = Number(document.getElementById("quiz-timer-input")?.value) || 20;
    const semester = Number(document.getElementById("quiz-semester-select")?.value) || 1;
    const negativeMarking = !!document.getElementById("quiz-negative-marking-input")?.checked;

    const btn = document.getElementById("save-quiz-btn");
    btn.disabled = true;

    try {
      if (activeQuizId) {
        await window.api.put(`/api/quizzes/${activeQuizId}`, {
          title,
          timeLimitMinutes,
          semester,
          negativeMarking
        });
        window.common.showToast("Quiz updated successfully!", "success");
      } else {
        await window.api.post(`/api/courses/${courseId}/quizzes`, {
          title,
          timeLimitMinutes,
          semester,
          negativeMarking
        });
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

/**
 * GUL AI Quiz Generator Handlers
 */
let generatedAiQuizData = null;

window.openAiQuizModal = function() {
  const topicInput = document.getElementById("ai-quiz-topic");
  if (topicInput && !topicInput.value) {
    const pageTitle = document.getElementById("quiz-page-course-title")?.innerText || "";
    topicInput.value = pageTitle.replace(/^Quizzes\s*·\s*/, "").trim();
  }
  document.getElementById("ai-generation-results-area").innerHTML = "";
  document.getElementById("btn-import-ai-quiz")?.classList.add("hidden");
  document.getElementById("ai-selected-count-text").innerText = "0 questions generated";
  window.common.openModal("ai-quiz-modal");
};

window.runAiQuizGeneration = async function() {
  const topic = document.getElementById("ai-quiz-topic")?.value.trim();
  if (!topic) {
    window.common.showToast("Please enter an academic topic", "warning");
    return;
  }

  const difficulty = document.getElementById("ai-quiz-difficulty")?.value || "Medium";
  const questionType = document.getElementById("ai-quiz-type")?.value || "MULTIPLE_CHOICE";
  const count = Number(document.getElementById("ai-quiz-count")?.value) || 5;

  const btn = document.getElementById("btn-run-ai-generate");
  const resultsArea = document.getElementById("ai-generation-results-area");
  const importBtn = document.getElementById("btn-import-ai-quiz");
  const countText = document.getElementById("ai-selected-count-text");

  btn.disabled = true;
  btn.innerText = "Generating...";
  resultsArea.innerHTML = `
    <div class="loading-container">
      <div class="spinner-primary"></div>
      <p>Consulting GUL Academic AI for ${count} ${difficulty} questions...</p>
    </div>
  `;

  try {
    const res = await window.api.post("/api/quizzes/generate-ai", {
      topic,
      difficulty,
      count,
      questionType
    });

    generatedAiQuizData = res;
    const questions = res.questions || [];

    if (questions.length === 0) {
      resultsArea.innerHTML = `<p class="text-muted" style="text-align: center; padding: 1.5rem;">No questions were returned. Try adjusting the topic.</p>`;
      return;
    }

    resultsArea.innerHTML = `
      <div style="margin-bottom: 0.75rem; display: flex; justify-content: space-between; align-items: center;">
        <h4 style="font-size: 0.95rem; font-weight: 700; color: #1e3a8a;">${window.common.escapeHTML(res.title || "Generated Questions")}</h4>
        <span class="badge badge-success">${questions.length} Questions Ready</span>
      </div>
      <div style="display: flex; flex-direction: column; gap: 0.75rem;">
        ${questions.map((q, qIdx) => `
          <div class="card" style="padding: 0.85rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); background: #ffffff;">
            <div style="display: flex; gap: 0.5rem; align-items: flex-start; margin-bottom: 0.5rem;">
              <input type="checkbox" class="ai-q-checkbox" data-qidx="${qIdx}" checked id="ai-q-check-${qIdx}" style="margin-top: 3px;" />
              <label for="ai-q-check-${qIdx}" style="font-weight: 600; font-size: 0.9rem; color: #0f172a; flex: 1;">
                ${qIdx + 1}. <input type="text" class="form-input ai-q-text" data-qidx="${qIdx}" value="${window.common.escapeHTML(q.questionText)}" style="display: inline-block; width: calc(100% - 2rem); margin-left: 0.25rem; font-weight: 600;" />
              </label>
            </div>
            <div style="margin-left: 1.5rem; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.4rem; font-size: 0.82rem;">
              ${(q.options || []).map((opt, oIdx) => `
                <div style="padding: 0.35rem 0.6rem; border-radius: 4px; border: 1px solid ${oIdx === q.correctOptionIndex ? "#86efac" : "#e2e8f0"}; background: ${oIdx === q.correctOptionIndex ? "#f0fdf4" : "#f8fafc"};">
                  ${oIdx === q.correctOptionIndex ? '<strong style="color: #16a34a;">✓ </strong>' : '○ '}
                  ${window.common.escapeHTML(opt)}
                </div>
              `).join("")}
            </div>
            ${q.explanation ? `
              <div style="margin-left: 1.5rem; margin-top: 0.4rem; font-size: 0.75rem; color: #64748b;">
                <strong>Explanation:</strong> ${window.common.escapeHTML(q.explanation)}
              </div>
            ` : ''}
          </div>
        `).join("")}
      </div>
    `;

    importBtn?.classList.remove("hidden");
    countText.innerText = `${questions.length} questions generated & selected`;
  } catch (err) {
    resultsArea.innerHTML = `
      <div style="padding: 1rem; color: #ef4444; background: #fef2f2; border: 1px solid #fecaca; border-radius: var(--radius-sm); font-size: 0.85rem;">
        Failed to generate questions: ${window.common.escapeHTML(err.message)}
      </div>
    `;
  } finally {
    btn.disabled = false;
    btn.innerText = "🚀 Generate";
  }
};

window.importAiQuestionsToQuiz = async function() {
  if (!generatedAiQuizData || !generatedAiQuizData.questions) return;

  const checkboxes = document.querySelectorAll(".ai-q-checkbox:checked");
  if (checkboxes.length === 0) {
    window.common.showToast("Please select at least one question to import", "warning");
    return;
  }

  const importBtn = document.getElementById("btn-import-ai-quiz");
  importBtn.disabled = true;
  importBtn.innerText = "Importing...";

  try {
    // Create new Quiz
    const quizTitle = generatedAiQuizData.title || `AI Assessment: ${document.getElementById("ai-quiz-topic")?.value.trim()}`;
    const newQuiz = await window.api.post(`/api/courses/${courseId}/quizzes`, {
      title: quizTitle,
      timeLimitMinutes: 20,
      negativeMarking: false,
      semester: 1
    });

    const quizId = newQuiz.id;

    // Import each checked question
    for (const cb of checkboxes) {
      const qIdx = Number(cb.getAttribute("data-qidx"));
      const originalQ = generatedAiQuizData.questions[qIdx];
      const editedTextInput = document.querySelector(`.ai-q-text[data-qidx="${qIdx}"]`);
      const qText = editedTextInput ? editedTextInput.value.trim() : originalQ.questionText;

      const formattedOptions = (originalQ.options || []).map((optText, oIdx) => ({
        optionText: optText,
        text: optText,
        isCorrect: oIdx === originalQ.correctOptionIndex,
        correct: oIdx === originalQ.correctOptionIndex
      }));

      await window.api.post(`/api/quizzes/${quizId}/questions`, {
        questionText: qText,
        text: qText,
        marks: originalQ.marks || 1,
        options: formattedOptions,
        correctOptionIndex: originalQ.correctOptionIndex,
        questionType: originalQ.questionType || "MULTIPLE_CHOICE",
        explanation: originalQ.explanation || ""
      });
    }

    window.common.showToast(`Imported ${checkboxes.length} questions into new quiz!`, "success");
    window.common.closeModal("ai-quiz-modal");
    await loadQuizzes();
  } catch (err) {
    window.common.showToast(err.message || "Failed to import AI questions", "error");
  } finally {
    importBtn.disabled = false;
    importBtn.innerText = "📥 Import Selected into Quiz";
  }
};
