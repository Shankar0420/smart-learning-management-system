/**
 * Galgotias LMS
 * Teacher Dashboard Controller
 * Connects to GET /api/teacher/dashboard
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "dashboard");

  const user = window.auth.getCurrentUser();
  if (user && user.name) {
    const idSuffix = user.professionalId ? ` (ID: ${user.professionalId})` : "";
    document.getElementById("teacher-welcome-heading").innerText = `Faculty Portal · Professor ${user.name}${idSuffix}`;
  }

  await loadTeacherDashboard();
  await loadTeacherAnnouncements();
  await loadAcademicCalendar();
  setupTeacherActionModals();
});

async function loadTeacherDashboard() {
  const statCourses = document.getElementById("stat-teacher-courses");
  const statMaterials = document.getElementById("stat-teacher-materials");
  const statQuizzes = document.getElementById("stat-teacher-quizzes");
  const statAssignments = document.getElementById("stat-teacher-assignments");
  const statStudents = document.getElementById("stat-teacher-students");
  const statSubmissions = document.getElementById("stat-teacher-submissions");
  const coursesContainer = document.getElementById("teacher-recent-courses");
  const materialsContainer = document.getElementById("teacher-recent-materials-container");
  const errorArea = document.getElementById("teacher-error-area");
  errorArea.classList.add("hidden");

  try {
    const data = await window.api.get("/api/teacher/dashboard");

    // All dynamic values strictly from backend
    if (statCourses) statCourses.innerText = data.totalCourses ?? 0;
    if (statMaterials) statMaterials.innerText = data.totalMaterials ?? 0;
    if (statQuizzes) statQuizzes.innerText = data.totalQuizzes ?? 0;
    if (statAssignments) statAssignments.innerText = data.totalAssignments ?? 0;
    if (statStudents) statStudents.innerText = data.totalStudents ?? 0;
    if (statSubmissions) statSubmissions.innerText = data.pendingSubmissions ?? 0;

    // Render Recent Study Materials
    const recentMaterials = data.recentMaterials || [];
    renderRecentMaterials(recentMaterials);

    // Render courses summary
    const allCourses = await window.api.get("/api/courses").catch(() => []);
    renderCoursesSummary(Array.isArray(allCourses) ? allCourses : (allCourses.courses || []));

  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(coursesContainer, error.message, () => loadTeacherDashboard());
  }
}

function renderRecentMaterials(materials) {
  const container = document.getElementById("teacher-recent-materials-container");
  if (!container) return;

  if (materials.length === 0) {
    window.common.renderEmptyState(
      container,
      "No Study Materials Uploaded",
      "Upload PPT, PPTX or PDF materials within your course modules.",
      '<a href="/teacher/courses.html" class="btn btn-primary btn-sm">Go to Courses</a>'
    );
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 80px;">Format</th>
            <th>Title & Description</th>
            <th>Course</th>
            <th>Size</th>
            <th>Uploaded Date</th>
            <th style="text-align: right;">Action</th>
          </tr>
        </thead>
        <tbody>
          ${materials.map(m => {
            const fileType = (m.fileType || "").toUpperCase();
            let badgeClass = "badge-info";
            if (fileType === "PDF") badgeClass = "badge-danger";
            else if (fileType === "PPT" || fileType === "PPTX") badgeClass = "badge-warning";

            const sizeKB = Math.round((m.fileSize || 0) / 1024);
            const sizeStr = sizeKB > 1024 ? (sizeKB / 1024).toFixed(1) + " MB" : sizeKB + " KB";

            return `
              <tr>
                <td><span class="badge ${badgeClass}" style="font-weight: 700;">${fileType || "DOC"}</span></td>
                <td>
                  <strong>${window.common.escapeHTML(m.title)}</strong>
                  ${m.description ? `<p class="text-muted" style="font-size: 0.8rem; margin: 0.2rem 0 0 0;">${window.common.escapeHTML(m.description)}</p>` : ''}
                </td>
                <td class="text-muted">${window.common.escapeHTML(m.courseTitle || "-")}</td>
                <td class="text-muted tabular-nums">${sizeStr}</td>
                <td class="text-muted tabular-nums">${window.common.formatDate(m.createdAt)}</td>
                <td style="text-align: right;">
                  <button class="btn btn-secondary btn-sm" onclick="handleDownloadTeacherMaterial('${m.id}', '${window.common.escapeHTML(m.originalFileName || 'material')}')">
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

function renderCoursesSummary(courses) {
  const container = document.getElementById("teacher-recent-courses");
  if (!container) return;

  if (courses.length === 0) {
    window.common.renderEmptyState(
      container,
      "No Courses Created",
      "You have not published any courses yet.",
      '<a href="/teacher/course-form.html" class="btn btn-primary btn-sm">+ Create Your First Course</a>'
    );
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>Course Title</th>
            <th>Description</th>
            <th>Status</th>
            <th style="text-align: right;">Actions</th>
          </tr>
        </thead>
        <tbody>
          ${courses.slice(0, 5).map(c => `
            <tr>
              <td><strong>${window.common.escapeHTML(c.title || "Untitled")}</strong></td>
              <td class="text-muted" style="max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                ${window.common.escapeHTML(c.description || "-")}
              </td>
              <td>
                <span class="badge ${c.active !== false ? 'badge-success' : 'badge-neutral'}">
                  ${c.active !== false ? 'Active' : 'Draft'}
                </span>
              </td>
              <td style="text-align: right;">
                <div class="flex items-center justify-end gap-2">
                  <a href="/teacher/course-details.html?id=${c.id}" class="btn btn-secondary btn-sm">Manage Curriculum</a>
                  <a href="/teacher/course-form.html?id=${c.id}" class="btn btn-outline btn-sm">Edit</a>
                </div>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

window.handleDownloadTeacherMaterial = async function(materialId, filename) {
  try {
    window.common.showToast("Downloading material...", "info");
    await window.api.download(`/api/materials/${materialId}/download`, filename);
  } catch (err) {
    window.common.showToast(err.message || "Failed to download material", "error");
  }
};

let teacherCoursesCache = [];

async function loadTeacherAnnouncements() {
  const container = document.getElementById("teacher-announcements-container");
  if (!container) return;

  try {
    const list = await window.api.get("/api/announcements").catch(() => []);
    if (!Array.isArray(list) || list.length === 0) {
      container.innerHTML = "";
      return;
    }

    container.innerHTML = `
      <div style="background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; box-shadow: var(--shadow-sm);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <h3 style="font-size: 1rem; font-weight: 700; color: var(--dark); display: flex; align-items: center; gap: 0.5rem;">
            📢 Active Course & Faculty Notices
          </h3>
          <span class="badge badge-info">${list.length} Notice${list.length > 1 ? 's' : ''}</span>
        </div>
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          ${list.slice(0, 3).map(a => `
            <div class="announcement-card priority-${window.common.escapeHTML(a.priority)}" style="margin-bottom: 0; padding: 1rem;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
                <strong style="font-size: 0.95rem; color: #0f172a;">${window.common.escapeHTML(a.title)}</strong>
                <span class="badge ${a.priority === 'URGENT' ? 'badge-danger' : (a.priority === 'HIGH' ? 'badge-warning' : 'badge-info')}">${window.common.escapeHTML(a.priority)}</span>
              </div>
              <p style="font-size: 0.85rem; color: #334155; margin-bottom: 0.5rem; line-height: 1.5;">${window.common.escapeHTML(a.content)}</p>
              <div style="font-size: 0.75rem; color: var(--text-secondary); display: flex; gap: 1rem;">
                <span>📍 ${window.common.escapeHTML(a.courseTitle || 'University-Wide')}</span>
                <span>👤 ${window.common.escapeHTML(a.authorName || 'Faculty')}</span>
                <span>⏱️ ${window.common.formatDate(a.createdAt)}</span>
              </div>
            </div>
          `).join("")}
        </div>
      </div>
    `;
  } catch (e) {
    console.warn("Could not load teacher announcements:", e);
  }
}

async function setupTeacherActionModals() {
  teacherCoursesCache = await window.api.get("/api/courses").catch(() => []);
  if (!Array.isArray(teacherCoursesCache)) {
    teacherCoursesCache = teacherCoursesCache.courses || [];
  }

  // Populate Select Boxes
  const annSelect = document.getElementById("ann-course-select");
  const attSelect = document.getElementById("att-course-select");

  const optionsHtml = teacherCoursesCache.map(c => `
    <option value="${c.id}">${window.common.escapeHTML(c.title)}</option>
  `).join("");

  if (annSelect) annSelect.innerHTML = optionsHtml;
  if (attSelect) attSelect.innerHTML = optionsHtml;

  // Set default date to today for attendance
  const attDate = document.getElementById("att-date");
  if (attDate) attDate.value = new Date().toISOString().split("T")[0];

  // Announcement Form Handler
  const annForm = document.getElementById("teacher-announcement-form");
  if (annForm) {
    annForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const courseId = document.getElementById("ann-course-select").value;
      const title = document.getElementById("ann-title").value.trim();
      const priority = document.getElementById("ann-priority").value;
      const content = document.getElementById("ann-content").value.trim();

      try {
        const btn = document.getElementById("ann-submit-btn");
        btn.disabled = true;
        btn.innerText = "Publishing...";

        await window.api.post("/api/announcements", {
          courseId: courseId ? Number(courseId) : null,
          title,
          priority,
          content,
          targetRole: "STUDENT"
        });

        window.common.showToast("Notice published successfully!", "success");
        window.common.closeModal("teacher-announcement-modal");
        annForm.reset();
        await loadTeacherAnnouncements();
      } catch (err) {
        window.common.showToast(err.message || "Failed to publish announcement", "error");
      } finally {
        const btn = document.getElementById("ann-submit-btn");
        if (btn) {
          btn.disabled = false;
          btn.innerText = "Publish Notice";
        }
      }
    });
  }

  // Attendance Form Handler
  const attForm = document.getElementById("teacher-attendance-form");
  if (attForm) {
    attForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const courseId = document.getElementById("att-course-select").value;
      const sessionDate = document.getElementById("att-date").value;
      const topic = document.getElementById("att-topic").value.trim();

      try {
        const btn = document.getElementById("att-submit-btn");
        btn.disabled = true;
        btn.innerText = "Recording...";

        await window.api.post(`/api/attendance/courses/${courseId}/sessions`, {
          sessionDate,
          topic
        });

        window.common.showToast("Attendance session recorded successfully!", "success");
        window.common.closeModal("teacher-attendance-modal");
        attForm.reset();
      } catch (err) {
        window.common.showToast(err.message || "Failed to record attendance session", "error");
      } finally {
        const btn = document.getElementById("att-submit-btn");
        if (btn) {
          btn.disabled = false;
          btn.innerText = "Save Session";
        }
      }
    });
  }
}

window.openTeacherAnnouncementModal = function() {
  window.common.openModal("teacher-announcement-modal");
};

window.openTeacherAttendanceModal = function() {
  window.common.openModal("teacher-attendance-modal");
};

/**
 * Academic Calendar Subsystem for Teachers
 */
async function loadAcademicCalendar() {
  const container = document.getElementById("teacher-calendar-container");
  if (!container) return;

  try {
    const events = await window.api.get("/api/calendar/events");
    const list = Array.isArray(events) ? events : [];

    if (list.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 1.5rem; color: #64748b; font-size: 0.85rem;">
          No academic events scheduled currently. Click "+ Add Academic Event" to publish exam dates and deadlines.
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem;">
        ${list.map(e => {
          const type = (e.eventType || "EVENT").toUpperCase();
          let badgeColor = "badge-info";
          let icon = "📌";
          if (type === "EXAM") { badgeColor = "badge-danger"; icon = "📝"; }
          else if (type === "HOLIDAY") { badgeColor = "badge-success"; icon = "🏖️"; }
          else if (type === "ASSIGNMENT_DEADLINE") { badgeColor = "badge-warning"; icon = "⏱️"; }
          else if (type === "INTERNAL_ASSESSMENT") { badgeColor = "badge-primary"; icon = "📊"; }

          return `
            <div style="padding: 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); background: #ffffff; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.4rem;">
                <span class="badge ${badgeColor}" style="font-size: 0.72rem;">${icon} ${type.replace(/_/g, " ")}</span>
                <button class="btn btn-danger-outline btn-sm" style="font-size: 0.7rem; padding: 0.1rem 0.35rem;" onclick="handleDeleteCalendarEvent(${e.id}, '${window.common.escapeHTML(e.title)}')">✕</button>
              </div>
              <h4 style="font-size: 0.95rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">
                ${window.common.escapeHTML(e.title)}
              </h4>
              <div style="font-size: 0.75rem; color: #64748b; margin-bottom: 0.4rem;">
                📅 ${window.common.formatDate(e.startDate)}
              </div>
              ${e.description ? `
                <p style="font-size: 0.8rem; color: #64748b; margin-bottom: 0.4rem; line-height: 1.35;">
                  ${window.common.escapeHTML(e.description)}
                </p>
              ` : ''}
              ${e.courseTitle ? `
                <div style="font-size: 0.72rem; color: #3b82f6; font-weight: 500;">
                  📚 ${window.common.escapeHTML(e.courseTitle)}
                </div>
              ` : ''}
            </div>
          `;
        }).join("")}
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p class="text-muted" style="font-size: 0.85rem;">Failed to load calendar events.</p>`;
  }
}

window.openAddEventModal = async function() {
  const select = document.getElementById("event-course-select");
  if (select) {
    try {
      const courses = await window.api.get("/api/courses").catch(() => []);
      select.innerHTML = '<option value="">General (All Students)</option>' +
        courses.map(c => `<option value="${c.id}">${window.common.escapeHTML(c.title)}</option>`).join("");
    } catch (e) {}
  }
  const form = document.getElementById("add-calendar-event-form");
  if (form) form.reset();
  window.common.openModal("add-calendar-event-modal");
};

window.handleDeleteCalendarEvent = function(id, title) {
  window.common.confirmAction(`Delete calendar event "${title}"?`, async () => {
    try {
      await window.api.delete(`/api/calendar/events/${id}`);
      window.common.showToast("Event removed from calendar", "success");
      await loadAcademicCalendar();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete event", "error");
    }
  });
};

// Form event listener for calendar event
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("add-calendar-event-form");
  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const title = document.getElementById("event-title-input")?.value.trim();
      const eventType = document.getElementById("event-type-select")?.value;
      const courseId = document.getElementById("event-course-select")?.value || null;
      const startDate = document.getElementById("event-start-date")?.value;
      const endDate = document.getElementById("event-end-date")?.value || null;
      const description = document.getElementById("event-desc-input")?.value.trim();

      const btn = document.getElementById("save-calendar-event-btn");
      if (btn) {
        btn.disabled = true;
        btn.innerText = "Publishing...";
      }

      try {
        await window.api.post("/api/calendar/events", {
          title,
          eventType,
          courseId: courseId ? Number(courseId) : null,
          startDate,
          endDate,
          description,
          allDay: false
        });

        window.common.showToast("Event scheduled on Academic Calendar!", "success");
        window.common.closeModal("add-calendar-event-modal");
        await loadAcademicCalendar();
      } catch (err) {
        window.common.showToast(err.message || "Failed to publish event", "error");
      } finally {
        if (btn) {
          btn.disabled = false;
          btn.innerText = "Publish to Calendar";
        }
      }
    });
  }
});
