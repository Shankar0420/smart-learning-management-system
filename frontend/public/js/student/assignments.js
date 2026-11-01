/**
 * Smart Learning Management System
 * Student Assignments Controller
 * Connects to GET /api/assignments and POST /api/assignments/{id}/submit
 */

let assignments = [];
let selectedAssignmentId = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "assignments");

  setupSubmissionForm();
  await loadAssignments();
});

async function loadAssignments() {
  const container = document.getElementById("assignments-list-container");
  const errorArea = document.getElementById("assignments-error-area");
  errorArea.classList.add("hidden");

  const params = new URLSearchParams(window.location.search);
  const courseId = params.get("courseId");

  try {
    let res;
    if (courseId) {
      res = await window.api.get(`/api/courses/${courseId}/assignments`).catch(async () => {
        return await window.api.get("/api/assignments");
      });
    } else {
      res = await window.api.get("/api/assignments").catch(async () => {
        // Fallback: fetch enrolled courses and query assignments
        const enrolled = await window.api.get("/api/student/courses").catch(() => []);
        const all = [];
        for (const item of (Array.isArray(enrolled) ? enrolled : (enrolled.courses || []))) {
          const cId = item.id || item.courseId;
          const cAssign = await window.api.get(`/api/courses/${cId}/assignments`).catch(() => []);
          all.push(...(Array.isArray(cAssign) ? cAssign : []));
        }
        return all;
      });
    }

    assignments = Array.isArray(res) ? res : (res.assignments || []);

    if (assignments.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Assignments Available",
        "There are no active assignments published for your enrolled courses."
      );
      return;
    }

    renderAssignments();
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadAssignments());
  }
}

function renderAssignments() {
  const container = document.getElementById("assignments-list-container");
  const now = new Date();

  container.innerHTML = `
    <div style="display: flex; flex-direction: column; gap: 1.25rem;">
      ${assignments.map(a => {
        const id = a.id;
        const title = a.title || "Assignment";
        const description = a.description || "Course assignment submission.";
        const courseName = a.courseName || a.course?.title || "University Course";
        const maxMarks = a.maxMarks || a.marks || 100;
        const deadlineDate = a.deadline ? new Date(a.deadline) : null;
        const isPastDeadline = deadlineDate && deadlineDate < now;
        const submitted = a.submitted || Boolean(a.submissionStatus === "SUBMITTED" || a.submissionStatus === "GRADED");
        const graded = a.submissionStatus === "GRADED" || a.marksObtained !== undefined || a.marks !== undefined;
        const score = a.marksObtained ?? a.marks;
        const feedback = a.feedback;

        return `
          <div class="card">
            <div class="card-body">
              <div class="flex items-center justify-between flex-wrap gap-3" style="margin-bottom: 0.75rem;">
                <div>
                  <span class="badge badge-info">${window.common.escapeHTML(courseName)}</span>
                  <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--dark); margin-top: 0.35rem;">
                    ${window.common.escapeHTML(title)}
                  </h3>
                </div>

                <div class="flex items-center gap-2">
                  ${graded ? `
                    <span class="badge badge-success">Graded: ${score}/${maxMarks}</span>
                  ` : submitted ? `
                    <span class="badge badge-warning">Submitted (Pending Review)</span>
                  ` : isPastDeadline ? `
                    <span class="badge badge-danger">Deadline Passed</span>
                  ` : `
                    <span class="badge badge-neutral">Not Submitted</span>
                  `}
                </div>
              </div>

              <p class="text-muted" style="font-size: 0.9rem; line-height: 1.6; margin-bottom: 1.25rem;">
                ${window.common.escapeHTML(description)}
              </p>

              <div class="flex items-center justify-between flex-wrap gap-4" style="padding-top: 1rem; border-top: 1px solid var(--border-color); font-size: 0.85rem;">
                <div class="flex items-center gap-4 text-muted">
                  <span>📅 Deadline: <strong class="${isPastDeadline ? 'text-danger' : 'text-primary'}">${window.common.formatDate(a.deadline)}</strong></span>
                  <span>💯 Max Marks: <strong>${maxMarks}</strong></span>
                </div>

                <div>
                  ${submitted ? `
                    <button class="btn btn-secondary btn-sm" onclick="openSubmissionModal('${id}', '${window.common.escapeHTML(title)}', '${a.deadline}', true)">
                      View Your Submission
                    </button>
                  ` : isPastDeadline ? `
                    <button class="btn btn-secondary btn-sm" disabled title="Submission closed by instructor">
                      Submission Closed
                    </button>
                  ` : `
                    <button class="btn btn-primary btn-sm" onclick="openSubmissionModal('${id}', '${window.common.escapeHTML(title)}', '${a.deadline}', false)">
                      Submit Assignment
                    </button>
                  `}
                </div>
              </div>

              ${feedback ? `
                <div class="alert alert-info" style="margin-top: 1rem; margin-bottom: 0;">
                  <span><strong>Instructor Feedback:</strong> ${window.common.escapeHTML(feedback)}</span>
                </div>
              ` : ""}
            </div>
          </div>
        `;
      }).join("")}
    </div>
  `;
}

window.openSubmissionModal = function(id, title, deadline, isViewOnly = false) {
  selectedAssignmentId = id;
  document.getElementById("modal-assignment-title").innerText = `Submit: ${title}`;
  document.getElementById("modal-deadline-text").innerText = window.common.formatDate(deadline);
  
  const textarea = document.getElementById("submission-content");
  const submitBtn = document.getElementById("submit-answer-btn");

  textarea.value = "";
  textarea.disabled = isViewOnly;
  if (isViewOnly) {
    submitBtn.classList.add("hidden");
  } else {
    submitBtn.classList.remove("hidden");
  }

  window.common.openModal("submit-assignment-modal");
};

function setupSubmissionForm() {
  const form = document.getElementById("assignment-submission-form");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!selectedAssignmentId) return;

    const textarea = document.getElementById("submission-content");
    const content = textarea.value.trim();
    if (!content) {
      window.common.showToast("Please enter your assignment solution text.", "error");
      return;
    }

    const submitBtn = document.getElementById("submit-answer-btn");
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Submitting...';

    try {
      await window.api.post(`/api/assignments/${selectedAssignmentId}/submit`, {
        content: content,
        submissionText: content
      });

      window.common.showToast("Assignment submitted successfully!", "success");
      window.common.closeModal("submit-assignment-modal");
      await loadAssignments();
    } catch (error) {
      window.common.showToast(error.message || "Failed to submit assignment", "error");
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerText = "Submit to Instructor";
    }
  });
}
