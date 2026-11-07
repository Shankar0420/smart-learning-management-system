/**
 * Smart Learning Management System
 * Student Dashboard Controller
 * Connects to GET /api/student/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  // Enforce Student Role
  if (!window.auth.requireRole("STUDENT")) return;

  // Initialize Layout
  window.common.initDashboardLayout("STUDENT", "dashboard");

  await loadStudentDashboard();
});

async function loadStudentDashboard() {
  const statEnrolled = document.getElementById("stat-enrolled");
  const statMaterials = document.getElementById("stat-materials");
  const statQuizzes = document.getElementById("stat-quizzes");
  const statAssignments = document.getElementById("stat-assignments");
  const statCompleted = document.getElementById("stat-completed");
  const statPending = document.getElementById("stat-pending");
  const progressContainer = document.getElementById("course-progress-container");
  const quizScoresContainer = document.getElementById("quiz-scores-container");
  const upcomingAssignmentsContainer = document.getElementById("upcoming-assignments-container");
  const recentMaterialsContainer = document.getElementById("recent-materials-container");
  const errorArea = document.getElementById("dashboard-error-area");

  try {
    const data = await window.api.get("/api/student/dashboard");
    const user = window.auth.getCurrentUser();

    // Display Student ID & Welcome
    const profId = data.professionalId || (user && user.professionalId);
    const userName = (user && user.name) || "Student";
    const idBadge = profId ? ` <span class="badge badge-info" style="font-size: 0.85rem; vertical-align: middle;">${profId}</span>` : "";
    document.getElementById("welcome-heading").innerHTML = `Welcome back, ${window.common.escapeHTML(userName)}!${idBadge}`;

    // Populate Statistics from Backend
    if (statEnrolled) statEnrolled.innerText = data.totalEnrolledCourses ?? 0;
    if (statMaterials) statMaterials.innerText = data.availableMaterialsCount ?? 0;
    if (statQuizzes) statQuizzes.innerText = data.publishedQuizzesCount ?? 0;
    if (statAssignments) statAssignments.innerText = data.totalAssignmentsCount ?? 0;
    if (statCompleted) statCompleted.innerText = data.completedLessons ?? 0;
    if (statPending) statPending.innerText = data.pendingAssignments ?? 0;

    // Render Course Progress
    const coursesProgress = data.courseProgress || [];
    if (coursesProgress.length === 0) {
      window.common.renderEmptyState(
        progressContainer,
        "No Enrolled Courses",
        "You have not enrolled in any courses yet.",
        '<a href="/student/courses.html" class="btn btn-primary btn-sm">Explore Courses</a>'
      );
    } else {
      progressContainer.innerHTML = coursesProgress.map(cp => {
        const title = cp.courseTitle || cp.title || "Course";
        const courseId = cp.courseId || cp.id;
        const percent = Math.min(100, Math.max(0, cp.percentage ?? cp.progress ?? 0));
        const completed = cp.completedLessons ?? 0;
        const total = cp.totalLessons ?? 0;

        return `
          <div style="margin-bottom: 1.25rem;">
            <div class="progress-header">
              <strong style="color: var(--dark);">${window.common.escapeHTML(title)}</strong>
              <span>${percent}% (${completed}/${total} lessons)</span>
            </div>
            <div class="progress-bar">
              <div class="progress-bar-fill ${percent === 100 ? 'success' : ''}" style="width: ${percent}%;"></div>
            </div>
            <div style="margin-top: 0.35rem; text-align: right;">
              <a href="/student/learning.html?courseId=${courseId}" style="font-size: 0.75rem;">Continue Learning →</a>
            </div>
          </div>
        `;
      }).join("");
    }

    // Render Quiz Scores & Results
    const quizScores = data.recentQuizScores || [];
    if (quizScores.length === 0) {
      window.common.renderEmptyState(quizScoresContainer, "No Quiz Attempts", "You haven't attempted any course quizzes yet.");
    } else {
      quizScoresContainer.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Quiz</th>
                <th>Course</th>
                <th>Score</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              ${quizScores.map(q => {
                const pct = q.totalMarks > 0 ? Math.round((q.score / q.totalMarks) * 100) : 0;
                const badgeClass = pct >= 50 ? "badge-success" : "badge-danger";
                return `
                  <tr>
                    <td><strong>${window.common.escapeHTML(q.quizTitle || "Quiz")}</strong></td>
                    <td class="text-muted">${window.common.escapeHTML(q.courseTitle || "-")}</td>
                    <td><span class="badge ${badgeClass}">${q.score}/${q.totalMarks} (${pct}%)</span></td>
                    <td class="text-muted tabular-nums">${window.common.formatDate(q.attemptedAt)}</td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      `;
    }

    // Render Upcoming Assignment Deadlines & Status
    const upcomingAssignments = data.upcomingAssignments || [];
    if (upcomingAssignments.length === 0) {
      window.common.renderEmptyState(upcomingAssignmentsContainer, "No Active Assignments", "All assignments are up to date!");
    } else {
      const now = new Date();
      upcomingAssignmentsContainer.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Assignment</th>
                <th>Course</th>
                <th>Deadline</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${upcomingAssignments.map(a => {
                const isPast = a.deadline ? new Date(a.deadline) < now : false;
                let statusBadge;
                if (a.graded) {
                  statusBadge = `<span class="badge badge-success">Graded: ${a.marksObtained}/${a.maxMarks}</span>`;
                } else if (a.submitted) {
                  statusBadge = `<span class="badge badge-warning">Submitted</span>`;
                } else if (isPast) {
                  statusBadge = `<span class="badge badge-danger">Deadline Passed</span>`;
                } else {
                  statusBadge = `<span class="badge badge-neutral">Not Submitted</span>`;
                }

                return `
                  <tr>
                    <td><strong>${window.common.escapeHTML(a.title || "Assignment")}</strong></td>
                    <td class="text-muted">${window.common.escapeHTML(a.courseTitle || "-")}</td>
                    <td class="${isPast && !a.submitted ? 'text-danger font-bold' : 'text-muted'} tabular-nums">
                      ${window.common.formatDate(a.deadline)}
                    </td>
                    <td>${statusBadge}</td>
                    <td>
                      <a href="/student/assignments.html" class="btn btn-secondary btn-sm">
                        ${a.submitted ? "View" : "Submit"}
                      </a>
                    </td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      `;
    }

    // Render Recent Study Materials
    const recentMaterials = data.recentMaterials || [];
    if (recentMaterials.length === 0) {
      window.common.renderEmptyState(recentMaterialsContainer, "No Materials Available", "No study materials have been uploaded for your courses.");
    } else {
      recentMaterialsContainer.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th style="width: 70px;">Format</th>
                <th>Material</th>
                <th>Course</th>
                <th>Size</th>
                <th style="text-align: right;">Action</th>
              </tr>
            </thead>
            <tbody>
              ${recentMaterials.map(m => {
                const fileType = (m.fileType || "").toUpperCase();
                let badgeClass = "badge-info";
                if (fileType === "PDF") badgeClass = "badge-danger";
                else if (fileType === "PPT" || fileType === "PPTX") badgeClass = "badge-warning";

                const sizeKB = Math.round((m.fileSize || 0) / 1024);
                const sizeStr = sizeKB > 1024 ? (sizeKB / 1024).toFixed(1) + " MB" : sizeKB + " KB";

                return `
                  <tr>
                    <td><span class="badge ${badgeClass}" style="font-size: 0.7rem;">${fileType || "DOC"}</span></td>
                    <td><strong>${window.common.escapeHTML(m.title)}</strong></td>
                    <td class="text-muted">${window.common.escapeHTML(m.courseTitle || "-")}</td>
                    <td class="text-muted tabular-nums">${sizeStr}</td>
                    <td style="text-align: right;">
                      <button class="btn btn-secondary btn-sm" onclick="handleDownloadDashboardMaterial('${m.id}', '${window.common.escapeHTML(m.title)}')">
                        ⬇ Download
                      </button>
                    </td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      `;
    }

  } catch (error) {
    if (errorArea) {
      errorArea.className = "alert alert-danger";
      errorArea.innerText = error.message;
      errorArea.classList.remove("hidden");
    }

    window.common.renderErrorState(progressContainer, error.message, () => loadStudentDashboard());
    window.common.renderErrorState(quizScoresContainer, error.message);
    window.common.renderErrorState(upcomingAssignmentsContainer, error.message);
    window.common.renderErrorState(recentMaterialsContainer, error.message);
  }
}

window.handleDownloadDashboardMaterial = async function(materialId, filename) {
  try {
    window.common.showToast("Downloading material...", "info");
    await window.api.download(`/api/materials/${materialId}/download`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download material", "error");
  }
};
