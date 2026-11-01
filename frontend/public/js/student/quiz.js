/**
 * Smart Learning Management System
 * Student Quiz Assessment Controller
 * Connects to GET /api/quizzes/{id}, POST /api/quizzes/{id}/submit, and GET /api/quizzes/{id}/attempts
 * NEVER exposes correct answers during quiz taking.
 */

let quizId = null;
let courseId = null;
let currentQuiz = null;
let userAnswers = {}; // { questionId: optionId }

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "courses");

  const params = new URLSearchParams(window.location.search);
  quizId = params.get("id") || params.get("quizId");
  courseId = params.get("courseId");

  if (!quizId && !courseId) {
    window.location.href = "/student/courses.html";
    return;
  }

  // If only courseId was provided, resolve the quiz for this course
  // Uses GET /api/courses/{courseId}/quizzes (the correct endpoint that checks enrollment)
  if (!quizId && courseId) {
    try {
      const quizzes = await window.api.get(`/api/courses/${courseId}/quizzes`);
      if (Array.isArray(quizzes) && quizzes.length > 0) {
        quizId = quizzes[0].id;
      } else {
        // No quizzes published for this course yet
        const errorArea = document.getElementById("quiz-error-area");
        const bodyContainer = document.getElementById("quiz-body-container");
        errorArea.innerText = "No quizzes are available for this course yet.";
        errorArea.classList.remove("hidden");
        window.common.renderEmptyState(bodyContainer, "No Quizzes Found", "The teacher has not published any quizzes for this course yet.");
        document.getElementById("quiz-submit-btn").disabled = true;
        return;
      }
    } catch (e) {
      const errorArea = document.getElementById("quiz-error-area");
      const bodyContainer = document.getElementById("quiz-body-container");
      errorArea.innerText = e.message || "Failed to load quizzes for this course.";
      errorArea.classList.remove("hidden");
      window.common.renderErrorState(bodyContainer, e.message || "Failed to load quizzes", () => window.location.reload());
      return;
    }
  }

  await loadQuizData();
  await loadQuizAttempts();
});

async function loadQuizData() {
  const bodyContainer = document.getElementById("quiz-body-container");
  const errorArea = document.getElementById("quiz-error-area");
  const titleEl = document.getElementById("quiz-title");
  const metaCount = document.getElementById("quiz-meta-count");
  const submitBtn = document.getElementById("quiz-submit-btn");

  errorArea.classList.add("hidden");

  try {
    currentQuiz = await window.api.get(`/api/quizzes/${quizId}`);

    titleEl.innerText = currentQuiz.title || "University Quiz Assessment";
    const questions = currentQuiz.questions || [];
    metaCount.innerText = `${questions.length} Questions`;

    if (questions.length === 0) {
      window.common.renderEmptyState(bodyContainer, "No Questions Found", "This quiz does not have any questions published yet.");
      submitBtn.disabled = true;
      return;
    }

    // Render questions with options. Correct answer is NEVER displayed.
    bodyContainer.innerHTML = questions.map((q, idx) => {
      const qId = q.id;
      const options = q.options || [];

      return `
        <div class="quiz-question-card" id="q-card-${qId}">
          <div class="quiz-question-header">
            <span style="font-weight: 600; color: var(--primary);">Question ${idx + 1} of ${questions.length}</span>
            <span class="badge badge-neutral">${q.marks || 1} mark${(q.marks || 1) > 1 ? 's' : ''}</span>
          </div>

          <div class="quiz-question-text">
            ${window.common.escapeHTML(q.questionText || q.text || "Question")}
          </div>

          <div class="quiz-options-list">
            ${options.map(opt => `
              <label class="quiz-option-label" for="opt-${opt.id}">
                <input
                  type="radio"
                  name="question-${qId}"
                  id="opt-${opt.id}"
                  value="${opt.id}"
                  onchange="handleSelectAnswer('${qId}', '${opt.id}')"
                />
                <span>${window.common.escapeHTML(opt.optionText || opt.text || "Option")}</span>
              </label>
            `).join("")}
          </div>
        </div>
      `;
    }).join("");

    submitBtn.disabled = false;
    submitBtn.onclick = handleSubmitQuiz;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(bodyContainer, error.message, () => loadQuizData());
  }
}

window.handleSelectAnswer = function(questionId, optionId) {
  userAnswers[questionId] = optionId;
};

async function handleSubmitQuiz() {
  const questions = currentQuiz.questions || [];
  const answeredCount = Object.keys(userAnswers).length;

  if (answeredCount < questions.length) {
    const proceed = confirm(`You have answered ${answeredCount} of ${questions.length} questions. Do you still wish to submit?`);
    if (!proceed) return;
  }

  const submitBtn = document.getElementById("quiz-submit-btn");
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner"></span> Grading Quiz...';

  try {
    // Send ONLY selected answers. NEVER send score, calculated marks, or answers marked correct.
    const payload = {
      answers: Object.entries(userAnswers).map(([qId, optId]) => ({
        questionId: Number(qId) || qId,
        selectedOptionId: Number(optId) || optId
      })),
      selectedAnswers: userAnswers
    };

    const result = await window.api.post(`/api/quizzes/${quizId}/submit`, payload);

    // Backend calculated score
    const score = result.score ?? result.marksObtained ?? 0;
    const totalMarks = result.totalMarks ?? result.maxMarks ?? questions.length;
    const feedback = result.feedback || result.message || "Your quiz submission has been recorded in MariaDB.";

    document.getElementById("result-score-display").innerText = `${score} / ${totalMarks}`;
    document.getElementById("result-feedback").innerText = feedback;
    document.getElementById("quiz-result-container").classList.remove("hidden");
    document.getElementById("quiz-main-card").classList.add("hidden");

    window.common.showToast("Quiz submitted and evaluated successfully!", "success");

    // Refresh attempts
    await loadQuizAttempts();
  } catch (error) {
    window.common.showToast(error.message || "Failed to submit quiz", "error");
    submitBtn.disabled = false;
    submitBtn.innerText = "Submit Answers for Grading";
  }
}

async function loadQuizAttempts() {
  const container = document.getElementById("quiz-attempts-container");
  if (!quizId) return;

  try {
    const attempts = await window.api.get(`/api/quizzes/${quizId}/attempts`).catch(() => []);
    const list = Array.isArray(attempts) ? attempts : (attempts.attempts || []);

    if (list.length === 0) {
      window.common.renderEmptyState(container, "No Quiz Attempts Yet", "Your completed attempt records will appear here.");
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Attempt #</th>
              <th>Score</th>
              <th>Percentage</th>
              <th>Attempt Date</th>
            </tr>
          </thead>
          <tbody>
            ${list.map((att, idx) => {
              const score = att.score ?? att.marksObtained ?? 0;
              const max = att.totalMarks ?? att.maxMarks ?? 100;
              const pct = max > 0 ? Math.round((score / max) * 100) : 0;
              return `
                <tr>
                  <td><strong>Attempt ${list.length - idx}</strong></td>
                  <td><span class="badge badge-success">${score} / ${max}</span></td>
                  <td class="tabular-nums">${pct}%</td>
                  <td class="text-muted">${window.common.formatDate(att.attemptDate || att.createdAt || att.date)}</td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    console.warn("Could not load previous attempts:", err);
    window.common.renderEmptyState(container, "No Attempts History", "Attempts data is currently unavailable.");
  }
}

window.retakeQuiz = function() {
  userAnswers = {};
  document.getElementById("quiz-result-container").classList.add("hidden");
  document.getElementById("quiz-main-card").classList.remove("hidden");
  loadQuizData();
};
