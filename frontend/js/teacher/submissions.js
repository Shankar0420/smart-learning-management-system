/**
 * Smart Learning Management System
 * Teacher Assignment Submissions & Grading Controller
 * Connects to:
 * GET /api/assignments/{id}/submissions
 * POST /api/assignments/submissions/{id}/grade
 */

let assignmentId = null;
let currentSubmissions = [];
let activeSubmission = null;
let currentMaxMarks = 100;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "submissions");

  const params = new URLSearchParams(window.location.search);
  assignmentId = params.get("assignmentId");

  setupGradingForm();
  await initAssignmentSelector();
});

async function initAssignmentSelector() {
  const select = document.getElementById("sub-assignment-select");

  try {
    const res = await window.api.get("/api/assignments").catch(() => []);
    const list = Array.isArray(res) ? res : (res.assignments || []);

    if (list.length === 0) {
      document.getElementById("assignment-filter-group").classList.add("hidden");
      if (assignmentId) {
        await loadSubmissions(assignmentId);
      } else {
        window.common.renderEmptyState(
          document.getElementById("submissions-table-container"),
          "No Assignments Found",
          "Create an assignment first before viewing student submissions."
        );
      }
      return;
    }

    select.innerHTML = list.map(a => `
      <option value="${a.id}" ${assignmentId && String(a.id) === String(assignmentId) ? "selected" : ""}>
        ${window.common.escapeHTML(a.title || "Assignment")}
      </option>
    `).join("");

    if (!assignmentId && list.length > 0) {
      assignmentId = list[0].id;
    }

    select.addEventListener("change", async (e) => {
      assignmentId = e.target.value;
      await loadSubmissions(assignmentId);
    });

    await loadSubmissions(assignmentId);
  } catch (err) {
    console.warn("Could not load assignment selector:", err);
    if (assignmentId) await loadSubmissions(assignmentId);
  }
}

async function loadSubmissions(targetAssignmentId) {
  const container = document.getElementById("submissions-table-container");
  const countBadge = document.getElementById("submissions-count-badge");
  const errorArea = document.getElementById("submissions-error-area");
  errorArea.classList.add("hidden");

  try {
    const res = await window.api.get(`/api/assignments/${targetAssignmentId}/submissions`);
    currentSubmissions = Array.isArray(res) ? res : (res.submissions || []);
    countBadge.innerText = `${currentSubmissions.length} Submission${currentSubmissions.length === 1 ? '' : 's'}`;

    if (currentSubmissions.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Submissions Yet",
        "Students have not submitted answers for this assignment yet."
      );
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Submission Preview</th>
              <th>Submitted Date</th>
              <th>Marks</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${currentSubmissions.map(sub => {
              const studentName = sub.studentName || sub.student?.name || sub.user?.name || "Student";
              const content = sub.content || sub.submissionText || "-";
              const marks = sub.marks ?? sub.marksObtained;
              const max = sub.maxMarks || 100;
              const isGraded = marks !== undefined && marks !== null;

              return `
                <tr>
                  <td><strong>${window.common.escapeHTML(studentName)}</strong></td>
                  <td class="text-muted" style="max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                    ${window.common.escapeHTML(content)}
                  </td>
                  <td class="text-muted tabular-nums">${window.common.formatDate(sub.submittedAt || sub.createdAt)}</td>
                  <td>
                    ${isGraded ? `<span class="badge badge-success">${marks}/${max}</span>` : '<span class="text-muted">Not graded</span>'}
                  </td>
                  <td>
                    <span class="badge ${isGraded ? 'badge-success' : 'badge-warning'}">
                      ${isGraded ? 'Graded' : 'Pending Review'}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-primary btn-sm" onclick='openGradeModal(${JSON.stringify(sub).replace(/'/g, "&apos;")})'>
                      ${isGraded ? 'Edit Grade' : 'Grade Submission'}
                    </button>
                  </td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadSubmissions(targetAssignmentId));
  }
}

window.openGradeModal = function(sub) {
  activeSubmission = sub;
  const studentName = sub.studentName || sub.student?.name || sub.user?.name || "Student";
  const content = sub.content || sub.submissionText || "No written text.";
  currentMaxMarks = sub.maxMarks || 100;

  document.getElementById("grade-modal-student").innerText = studentName;
  document.getElementById("grade-modal-submitted-at").innerText = window.common.formatDate(sub.submittedAt || sub.createdAt);
  document.getElementById("grade-modal-content").innerText = content;
  document.getElementById("grade-modal-max-marks").innerText = currentMaxMarks;

  const marksInput = document.getElementById("grade-marks-input");
  marksInput.max = currentMaxMarks;
  marksInput.value = sub.marks ?? sub.marksObtained ?? "";

  document.getElementById("grade-feedback-input").value = sub.feedback || "";

  window.common.openModal("grading-modal");
};

function setupGradingForm() {
  document.getElementById("grading-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!activeSubmission) return;

    const marksInput = document.getElementById("grade-marks-input");
    const feedbackInput = document.getElementById("grade-feedback-input");
    const marks = Number(marksInput.value);
    const feedback = feedbackInput.value.trim();

    // Client-side validation: no negative marks or marks > maxMarks
    if (isNaN(marks) || marks < 0) {
      window.common.showToast("Marks cannot be negative.", "error");
      return;
    }
    if (marks > currentMaxMarks) {
      window.common.showToast(`Marks cannot exceed the maximum of ${currentMaxMarks}.`, "error");
      return;
    }
    if (!feedback) {
      window.common.showToast("Please provide constructive feedback.", "error");
      return;
    }

    const btn = document.getElementById("submit-grade-btn");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Saving Grade...';

    try {
      await window.api.post(`/api/assignments/submissions/${activeSubmission.id}/grade`, {
        marks,
        feedback
      });

      window.common.showToast("Grade and feedback saved successfully!", "success");
      window.common.closeModal("grading-modal");
      await loadSubmissions(assignmentId);
    } catch (err) {
      window.common.showToast(err.message || "Failed to submit grade", "error");
    } finally {
      btn.disabled = false;
      btn.innerText = "Save Grade & Feedback";
    }
  });
}
