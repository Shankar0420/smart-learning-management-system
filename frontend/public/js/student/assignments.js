/**
 * Smart Learning Management System
 * Student Assignments Controller
 * Connects to GET /api/assignments, POST /api/assignments/{id}/submit,
 * POST /api/assignments/{id}/submit-file, and GET /api/assignments/{id}/attachment
 */

let assignments = [];
let selectedAssignmentId = null;
let currentSubmission = null;

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

    const rawAssignments = Array.isArray(res) ? res : (res.assignments || []);

    // Merge submission statuses from student dashboard
    const dashboardData = await window.api.get("/api/student/dashboard").catch(() => null);
    const subMap = new Map();
    if (dashboardData && dashboardData.upcomingAssignments) {
      dashboardData.upcomingAssignments.forEach(item => {
        subMap.set(String(item.id), item);
      });
    }

    assignments = rawAssignments.map(a => {
      const subInfo = subMap.get(String(a.id));
      if (subInfo) {
        return {
          ...a,
          submitted: subInfo.submitted,
          graded: subInfo.graded,
          marksObtained: subInfo.marksObtained,
          submissionStatus: subInfo.graded ? "GRADED" : (subInfo.submitted ? "SUBMITTED" : "PENDING")
        };
      }
      return a;
    });

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
        const graded = a.graded || a.submissionStatus === "GRADED" || a.marksObtained !== undefined && a.marksObtained !== null;
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

              ${a.hasAttachment ? `
                <div style="margin-bottom: 1.25rem; display: flex; align-items: center; gap: 0.75rem; background: var(--bg-alt); padding: 0.6rem 0.85rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
                  <span style="font-size: 0.85rem;">📎 Supporting Document: <strong>${window.common.escapeHTML(a.attachmentOriginalName || 'Download attachment')}</strong></span>
                  <button type="button" class="btn btn-secondary btn-sm" onclick="handleDownloadAttachment('${id}', '${window.common.escapeHTML(a.attachmentOriginalName || 'attachment')}')">
                    ⬇ Download Specs
                  </button>
                </div>
              ` : ''}

              <div class="flex items-center justify-between flex-wrap gap-4" style="padding-top: 1rem; border-top: 1px solid var(--border-color); font-size: 0.85rem;">
                <div class="flex items-center gap-4 text-muted">
                  <span>📅 Deadline: <strong class="${isPastDeadline ? 'text-danger' : 'text-primary'}">${window.common.formatDate(a.deadline)}</strong></span>
                  <span>💯 Max Marks: <strong>${maxMarks}</strong></span>
                </div>

                <div>
                  ${submitted ? `
                    <button class="btn btn-secondary btn-sm" onclick="openSubmissionModal('${id}', true)">
                      View Your Submission
                    </button>
                  ` : isPastDeadline ? `
                    <button class="btn btn-secondary btn-sm" disabled title="Submission closed by instructor">
                      Submission Closed
                    </button>
                  ` : `
                    <button class="btn btn-primary btn-sm" onclick="openSubmissionModal('${id}', false)">
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

window.openSubmissionModal = async function(id, isViewOnly = false) {
  selectedAssignmentId = id;
  const a = assignments.find(item => String(item.id) === String(id));
  if (!a) return;

  const title = a.title || "Assignment";
  document.getElementById("modal-assignment-title").innerText = isViewOnly ? `Submission: ${title}` : `Submit: ${title}`;
  document.getElementById("modal-deadline-text").innerText = window.common.formatDate(a.deadline);

  // Attachment notice
  const attContainer = document.getElementById("modal-attachment-container");
  const attDlBtn = document.getElementById("modal-attachment-dl-btn");
  if (a.hasAttachment) {
    attContainer.classList.remove("hidden");
    document.getElementById("modal-attachment-text").innerText = `📎 Supporting Document: ${a.attachmentOriginalName || "Specs"}`;
    attDlBtn.onclick = () => window.handleDownloadAttachment(a.id, a.attachmentOriginalName || "attachment");
  } else {
    attContainer.classList.add("hidden");
  }

  // Load existing submission if any
  currentSubmission = null;
  const subFileArea = document.getElementById("modal-submitted-file-area");
  const subFileDlBtn = document.getElementById("modal-submitted-file-dl-btn");
  const fileInputGroup = document.getElementById("submission-file-group");
  const fileInput = document.getElementById("submission-file");
  const textarea = document.getElementById("submission-content");
  const submitBtn = document.getElementById("submit-answer-btn");

  fileInput.value = "";
  textarea.value = "";

  try {
    const sub = await window.api.get(`/api/assignments/${id}/my-submission`).catch(() => null);
    if (sub) {
      currentSubmission = sub;
      textarea.value = sub.answerText || "";
      if (sub.hasSubmissionFile) {
        subFileArea.classList.remove("hidden");
        document.getElementById("modal-submitted-file-name").innerText = `📄 Submitted File: ${sub.submissionOriginalName || "File"}`;
        subFileDlBtn.onclick = () => window.handleDownloadSubmissionFile(sub.id, sub.submissionOriginalName || "submission");
      } else {
        subFileArea.classList.add("hidden");
      }
    } else {
      subFileArea.classList.add("hidden");
    }
  } catch (err) {
    console.warn("Could not check submission:", err);
    subFileArea.classList.add("hidden");
  }

  if (isViewOnly) {
    fileInputGroup.classList.add("hidden");
    textarea.disabled = true;
    submitBtn.classList.add("hidden");
  } else {
    fileInputGroup.classList.remove("hidden");
    textarea.disabled = false;
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
    const fileInput = document.getElementById("submission-file");
    const content = textarea.value.trim();
    const hasFile = fileInput.files && fileInput.files.length > 0;

    if (!content && !hasFile) {
      window.common.showToast("Please provide written answer text or upload a solution document.", "error");
      return;
    }

    const submitBtn = document.getElementById("submit-answer-btn");
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Submitting...';

    try {
      if (hasFile) {
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);
        if (content) {
          formData.append("answerText", content);
        }
        await window.api.post(`/api/assignments/${selectedAssignmentId}/submit-file`, formData);
      } else {
        await window.api.post(`/api/assignments/${selectedAssignmentId}/submit`, {
          content: content,
          submissionText: content
        });
      }

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

window.handleDownloadAttachment = async function(assignmentId, filename) {
  try {
    window.common.showToast("Downloading attachment...", "info");
    await window.api.download(`/api/assignments/${assignmentId}/attachment`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download attachment", "error");
  }
};

window.handleDownloadSubmissionFile = async function(submissionId, filename) {
  try {
    window.common.showToast("Downloading your submission file...", "info");
    await window.api.download(`/api/assignments/submissions/${submissionId}/file`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download submission file", "error");
  }
};
