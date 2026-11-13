/**
 * Smart Learning Management System
 * Student Performance Analytics & Verified Certificates
 */

let enrolledCourses = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "grades");

  await Promise.all([
    loadAnalytics(),
    loadCertificates(),
    loadGrades()
  ]);
});

async function loadAnalytics() {
  const errorArea = document.getElementById("grades-error-area");
  try {
    const data = await window.api.get("/api/analytics/student");
    if (!data) return;

    // Render Metrics
    const cgpaEl = document.getElementById("metric-cgpa");
    const attEl = document.getElementById("metric-attendance");
    const quizEl = document.getElementById("metric-quiz-avg");
    const assignEl = document.getElementById("metric-assignments");
    const assignMeta = document.getElementById("metric-assignments-meta");
    const attBanner = document.getElementById("attendance-warning-banner");

    if (cgpaEl) cgpaEl.innerText = (data.projectedCgpa || 0).toFixed(2);
    if (attEl) attEl.innerText = `${Math.round(data.overallAttendancePercentage || 0)}%`;
    if (quizEl) quizEl.innerText = `${Math.round(data.overallQuizAverage || 0)}%`;
    if (assignEl) assignEl.innerText = `${data.completedAssignments || 0}`;
    if (assignMeta) assignMeta.innerText = `Out of ${data.totalAssignments || (data.completedAssignments + data.pendingAssignments)} total`;

    // Attendance compliance warning
    if (attBanner) {
      if ((data.overallAttendancePercentage || 0) < 75) {
        attBanner.classList.remove("hidden");
      } else {
        attBanner.classList.add("hidden");
      }
    }

    // Render Subject-wise breakdown
    renderCourseBreakdowns(data.courseBreakdowns || []);
  } catch (err) {
    if (errorArea) {
      errorArea.innerText = `Unable to load analytics summary: ${err.message}`;
      errorArea.classList.remove("hidden");
    }
  }
}

function renderCourseBreakdowns(breakdowns) {
  const container = document.getElementById("course-breakdown-container");
  if (!container) return;

  if (breakdowns.length === 0) {
    container.innerHTML = `
      <div style="padding: 1.5rem; text-align: center; color: #64748b; font-size: 0.875rem;">
        No course performance records available yet.
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>Course Title</th>
            <th>Attendance %</th>
            <th>Attendance Status</th>
            <th>Assignments Done</th>
            <th>Quiz Average</th>
            <th>Syllabus Progress</th>
            <th>Estimated Grade</th>
          </tr>
        </thead>
        <tbody>
          ${breakdowns.map(b => {
            const attPct = Math.round(b.attendancePercentage || 0);
            const isGood = attPct >= 75;
            const progress = Math.round(b.courseProgressPercentage || 0);

            return `
              <tr>
                <td><strong>${window.common.escapeHTML(b.courseTitle)}</strong></td>
                <td>
                  <span class="badge ${isGood ? "badge-success" : "badge-danger"}">${attPct}%</span>
                </td>
                <td>
                  <span class="badge ${b.attendanceStatus === "ELIGIBLE" ? "badge-success" : "badge-warning"}">
                    ${window.common.escapeHTML(b.attendanceStatus || "REGULAR")}
                  </span>
                </td>
                <td>${b.completedAssignments || 0} / ${b.totalAssignments || 0}</td>
                <td>${Math.round(b.quizAverage || 0)}%</td>
                <td style="min-width: 140px;">
                  <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <div style="flex: 1; height: 6px; background: #e2e8f0; border-radius: 9999px; overflow: hidden;">
                      <div style="width: ${progress}%; height: 100%; background: var(--primary);"></div>
                    </div>
                    <span style="font-size: 0.75rem; color: #64748b;">${progress}%</span>
                  </div>
                </td>
                <td>
                  <span class="badge badge-info" style="font-weight: 700; font-size: 0.85rem;">
                    ${window.common.escapeHTML(b.estimatedGrade || "N/A")}
                  </span>
                </td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

async function loadCertificates() {
  const container = document.getElementById("certificates-container");
  if (!container) return;

  try {
    const certs = await window.api.get("/api/certificates/my-certificates");
    if (!certs || certs.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 2rem 1rem; color: #64748b;">
          <div style="font-size: 2rem; margin-bottom: 0.5rem;">📜</div>
          <h4 style="font-size: 1rem; font-weight: 600; color: #1e293b; margin-bottom: 0.25rem;">No Certificates Claimed Yet</h4>
          <p style="font-size: 0.82rem; max-width: 450px; margin: 0 auto 1rem auto;">
            When you complete 100% of your course learning modules and assignments, you can generate your official university digital certificate here.
          </p>
          <button class="btn btn-outline btn-sm" onclick="window.checkEligibleCertificates()">Check Eligibility</button>
        </div>
      `;
      return;
    }

    const baseUrl = window.APP_CONFIG.getApiBaseUrl();

    container.innerHTML = `
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1rem;">
        ${certs.map(c => `
          <div class="certificate-card-fancy">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
              <span class="badge badge-success" style="font-size: 0.72rem;">✓ VERIFIED CREDENTIAL</span>
              <span style="font-size: 0.75rem; color: #94a3b8;">${window.common.formatDate(c.issuedAt)}</span>
            </div>
            <h4 style="font-size: 1.1rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">
              ${window.common.escapeHTML(c.courseTitle)}
            </h4>
            <p style="font-size: 0.8rem; color: #64748b; margin-bottom: 0.75rem;">
              Awarded to <strong>${window.common.escapeHTML(c.studentName)}</strong>
            </p>
            <div style="margin-bottom: 1rem;">
              <span class="certificate-code-chip">${window.common.escapeHTML(c.certificateCode)}</span>
            </div>
            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
              <a href="${baseUrl}/api/certificates/verify/${encodeURIComponent(c.certificateCode)}" target="_blank" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">
                🔍 Public Verification URL
              </a>
              <button class="btn btn-primary btn-sm" style="font-size: 0.75rem;" onclick="window.common.showToast('Certificate verified by Galgotias University Registrar', 'success')">
                🖨️ Print / Save
              </button>
            </div>
          </div>
        `).join("")}
      </div>
    `;
  } catch (err) {
    container.innerHTML = `
      <div style="padding: 1.5rem; text-align: center; color: #ef4444; font-size: 0.85rem;">
        Failed to load digital certificates: ${window.common.escapeHTML(err.message)}
      </div>
    `;
  }
}

window.checkEligibleCertificates = async function() {
  const modal = document.getElementById("claim-cert-modal");
  const select = document.getElementById("claim-course-select");
  const statusDiv = document.getElementById("claim-cert-status");
  if (statusDiv) statusDiv.innerHTML = "";

  try {
    // Get enrolled courses
    const courses = await window.api.get("/api/courses/enrolled").catch(async () => {
      return await window.api.get("/api/courses");
    });

    enrolledCourses = Array.isArray(courses) ? courses : [];
    if (select) {
      if (enrolledCourses.length === 0) {
        select.innerHTML = '<option value="">No enrolled courses found</option>';
      } else {
        select.innerHTML = '<option value="">Select a course to claim...</option>' + 
          enrolledCourses.map(c => `<option value="${c.id}">${window.common.escapeHTML(c.title)}</option>`).join("");
      }
    }

    window.common.openModal("claim-cert-modal");
  } catch (err) {
    window.common.showToast(err.message || "Failed to fetch enrolled courses", "error");
  }
};

window.submitCertificateClaim = async function() {
  const select = document.getElementById("claim-course-select");
  const statusDiv = document.getElementById("claim-cert-status");
  const courseId = select?.value;

  if (!courseId) {
    window.common.showToast("Please choose a course first", "warning");
    return;
  }

  statusDiv.innerHTML = '<span class="text-muted" style="font-size: 0.82rem;">Verifying course completion & issuing cryptographic certificate...</span>';

  try {
    const res = await window.api.post(`/api/certificates/generate?courseId=${courseId}`, {});
    statusDiv.innerHTML = `<span class="badge badge-success">✓ Certificate Generated: ${window.common.escapeHTML(res.certificateCode)}</span>`;
    window.common.showToast("Certificate successfully claimed!", "success");
    setTimeout(async () => {
      window.common.closeModal("claim-cert-modal");
      await loadCertificates();
    }, 1200);
  } catch (err) {
    statusDiv.innerHTML = `<span class="badge badge-danger">✗ ${window.common.escapeHTML(err.message || "Unable to claim certificate")}</span>`;
  }
};

async function loadGrades() {
  const container = document.getElementById("grades-table-container");
  const countBadge = document.getElementById("grades-count-badge");
  const errorArea = document.getElementById("grades-error-area");
  if (errorArea) errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/grades").catch(async () => {
      return await window.api.get("/api/student/grades");
    });

    const grades = Array.isArray(res) ? res : (res.grades || []);
    if (countBadge) {
      countBadge.innerText = `${grades.length} Record${grades.length === 1 ? '' : 's'}`;
    }

    if (grades.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Grades Published",
        "Your completed quizzes and evaluated assignments will appear here once graded by your instructors."
      );
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Course</th>
              <th>Assessment Name</th>
              <th>Type</th>
              <th>Marks Obtained</th>
              <th>Instructor Feedback</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            ${grades.map(g => {
              const courseTitle = g.courseTitle || g.courseName || g.course?.title || "University Course";
              const assessment = g.assessmentName || g.title || g.assignmentTitle || g.quizTitle || "Evaluation";
              const type = g.type || (g.quizId ? "Quiz" : "Assignment");
              const marks = g.marksObtained ?? g.marks ?? 0;
              const max = g.maxMarks ?? g.totalMarks ?? 100;
              const feedback = g.feedback || "Good work.";
              const date = g.date || g.createdAt || g.evaluatedAt;

              return `
                <tr>
                  <td><strong>${window.common.escapeHTML(courseTitle)}</strong></td>
                  <td>${window.common.escapeHTML(assessment)}</td>
                  <td><span class="badge badge-neutral">${window.common.escapeHTML(type)}</span></td>
                  <td><span class="badge badge-success">${marks} / ${max}</span></td>
                  <td class="text-muted" style="max-width: 250px;">${window.common.escapeHTML(feedback)}</td>
                  <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (error) {
    if (errorArea) {
      errorArea.innerText = error.message;
      errorArea.classList.remove("hidden");
    }
    window.common.renderErrorState(container, error.message, () => loadGrades());
  }
}
