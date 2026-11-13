/**
 * Smart Learning Management System
 * Student Grades Controller
 * Connects to GET /api/grades
 * Read-only interface for students.
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "grades");

  await loadGrades();
});

async function loadGrades() {
  const container = document.getElementById("grades-table-container");
  const countBadge = document.getElementById("grades-count-badge");
  const errorArea = document.getElementById("grades-error-area");
  errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/grades").catch(async () => {
      // Fallback endpoint if named /api/student/grades in backend
      return await window.api.get("/api/student/grades");
    });

    const grades = Array.isArray(res) ? res : (res.grades || []);
    countBadge.innerText = `${grades.length} Record${grades.length === 1 ? '' : 's'}`;

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
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadGrades());
  }
}
