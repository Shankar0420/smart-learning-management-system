/**
 * Smart Learning Management System
 * Admin Dashboard Controller
 * Connects to:
 * - GET /api/admin/dashboard
 * - GET /api/admin/teachers/pending
 * - PUT /api/admin/teachers/{id}/approve
 * - PUT /api/admin/teachers/{id}/reject
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "dashboard");

  await loadAdminDashboard();
  await loadPendingTeachers();
  await loadPendingStudents();
  await loadAdminAnnouncements();
  await loadAuditLogs();
  setupAdminAnnouncementModal();
});

async function loadAdminDashboard() {
  const statStudents = document.getElementById("stat-admin-students");
  const statTeachers = document.getElementById("stat-admin-teachers");
  const statCourses = document.getElementById("stat-admin-courses");
  const statEnrollments = document.getElementById("stat-admin-enrollments");
  const statPendingTeachers = document.getElementById("stat-admin-pending-teachers");
  const statPendingStudents = document.getElementById("stat-admin-pending-students");
  const statActive = document.getElementById("stat-admin-active");
  const statInactive = document.getElementById("stat-admin-inactive");
  const errorArea = document.getElementById("admin-error-area");
  if (errorArea) errorArea.classList.add("hidden");

  try {
    const data = await window.api.get("/api/admin/dashboard");

    if (statStudents) statStudents.innerText = data.totalStudents ?? data.studentsCount ?? 0;
    if (statTeachers) statTeachers.innerText = data.totalTeachers ?? data.teachersCount ?? 0;
    if (statCourses) statCourses.innerText = data.totalCourses ?? data.coursesCount ?? 0;
    if (statEnrollments) statEnrollments.innerText = data.totalEnrollments ?? data.enrollmentsCount ?? 0;
    if (statPendingTeachers) statPendingTeachers.innerText = data.pendingTeachers ?? 0;
    if (statPendingStudents) statPendingStudents.innerText = data.pendingStudents ?? 0;
    if (statActive) statActive.innerText = data.activeAccounts ?? 0;
    if (statInactive) statInactive.innerText = data.inactiveAccounts ?? 0;
  } catch (error) {
    if (errorArea) {
      errorArea.innerText = error.message;
      errorArea.classList.remove("hidden");
    }
  }
}

async function loadPendingTeachers() {
  const container = document.getElementById("pending-teachers-table-container");
  const badge = document.getElementById("pending-teachers-count-badge");
  const statPendingTeachers = document.getElementById("stat-admin-pending-teachers");
  if (!container) return;

  try {
    const pendingList = await window.api.get("/api/admin/teachers/pending");
    const count = Array.isArray(pendingList) ? pendingList.length : 0;

    if (badge) badge.innerText = `${count} Pending`;
    if (statPendingTeachers) statPendingTeachers.innerText = count;

    if (count === 0) {
      window.common.renderEmptyState(
        container,
        "No Pending Applications",
        "There are currently no teacher registrations awaiting administrator review."
      );
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Teacher Name</th>
              <th>University Email</th>
              <th>Employee ID</th>
              <th>Submitted Date</th>
              <th>Approval Status</th>
              <th style="text-align: right;">Review Actions</th>
            </tr>
          </thead>
          <tbody>
            ${pendingList.map(teacher => {
              const id = teacher.id;
              const name = teacher.name || "Teacher";
              const email = teacher.email || "-";
              const empId = teacher.employeeId || "N/A";
              const date = teacher.createdAt;

              return `
                <tr>
                  <td><strong>${window.common.escapeHTML(name)}</strong></td>
                  <td class="text-muted">${window.common.escapeHTML(email)}</td>
                  <td>
                    <code style="background: rgba(37, 99, 235, 0.08); color: var(--primary); padding: 0.15rem 0.4rem; border-radius: 4px; font-weight: 600;">
                      ${window.common.escapeHTML(empId)}
                    </code>
                  </td>
                  <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                  <td>
                    <span class="badge badge-warning">Pending Approval</span>
                  </td>
                  <td style="text-align: right;">
                    <div class="flex items-center justify-end gap-2">
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none;"
                        onclick="handleApproveTeacher('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                      <button
                        class="btn btn-danger-outline btn-sm"
                        onclick="handleRejectTeacher('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✕ Reject
                      </button>
                    </div>
                  </td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    window.common.renderErrorState(container, err.message, () => loadPendingTeachers());
  }
}

window.handleApproveTeacher = async function(id, name) {
  window.common.confirmAction(`Are you sure you want to approve faculty registration for ${name}?`, async () => {
    try {
      await window.api.put(`/api/admin/teachers/${id}/approve`, {});
      window.common.showToast(`Faculty account for ${name} has been approved successfully!`, "success");
      await loadAdminDashboard();
      await loadPendingTeachers();
    } catch (err) {
      window.common.showToast(err.message || "Failed to approve teacher", "error");
    }
  });
};

window.handleRejectTeacher = async function(id, name) {
  window.common.confirmAction(`Are you sure you want to reject faculty registration for ${name}?`, async () => {
    try {
      await window.api.put(`/api/admin/teachers/${id}/reject`, {});
      window.common.showToast(`Faculty registration for ${name} has been rejected.`, "info");
      await loadAdminDashboard();
      await loadPendingTeachers();
    } catch (err) {
      window.common.showToast(err.message || "Failed to reject teacher", "error");
    }
  });
};

async function loadPendingStudents() {
  const container = document.getElementById("pending-students-table-container");
  const badge = document.getElementById("pending-students-count-badge");
  const statPendingStudents = document.getElementById("stat-admin-pending-students");
  if (!container) return;

  try {
    const pendingList = await window.api.get("/api/admin/students/pending");
    const count = Array.isArray(pendingList) ? pendingList.length : 0;

    if (badge) badge.innerText = `${count} Pending`;
    if (statPendingStudents) statPendingStudents.innerText = count;

    if (count === 0) {
      window.common.renderEmptyState(
        container,
        "No Pending Student Applications",
        "There are currently no student registrations awaiting administrator review."
      );
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Student Name</th>
              <th>University Email</th>
              <th>Department / Enrollment</th>
              <th>Submitted Date</th>
              <th>Approval Status</th>
              <th style="text-align: right;">Review Actions</th>
            </tr>
          </thead>
          <tbody>
            ${pendingList.map(student => {
              const id = student.id;
              const name = student.name || "Student";
              const email = student.email || "-";
              const enrollment = student.enrollmentNumber || student.department || "Standard Admission";
              const date = student.createdAt;

              return `
                <tr>
                  <td><strong>${window.common.escapeHTML(name)}</strong></td>
                  <td class="text-muted">${window.common.escapeHTML(email)}</td>
                  <td>
                    <code style="background: rgba(37, 99, 235, 0.08); color: var(--primary); padding: 0.15rem 0.4rem; border-radius: 4px; font-weight: 600;">
                      ${window.common.escapeHTML(enrollment)}
                    </code>
                  </td>
                  <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                  <td>
                    <span class="badge badge-warning">Pending Approval</span>
                  </td>
                  <td style="text-align: right;">
                    <div class="flex items-center justify-end gap-2">
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none;"
                        onclick="handleApproveStudent('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                      <button
                        class="btn btn-danger-outline btn-sm"
                        onclick="handleRejectStudent('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✕ Reject
                      </button>
                    </div>
                  </td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    window.common.renderErrorState(container, err.message, () => loadPendingStudents());
  }
}

window.handleApproveStudent = async function(id, name) {
  window.common.confirmAction(`Are you sure you want to approve student registration for ${name}? A unique Student ID (GUL-STU-2026-XXX) will be assigned.`, async () => {
    try {
      await window.api.put(`/api/admin/students/${id}/approve`, {});
      window.common.showToast(`Student account for ${name} approved and activated!`, "success");
      await loadAdminDashboard();
      await loadPendingStudents();
    } catch (err) {
      window.common.showToast(err.message || "Failed to approve student", "error");
    }
  });
};

window.handleRejectStudent = async function(id, name) {
  window.common.confirmAction(`Are you sure you want to reject student registration for ${name}?`, async () => {
    try {
      await window.api.put(`/api/admin/students/${id}/reject`, {});
      window.common.showToast(`Student registration for ${name} has been rejected.`, "info");
      await loadAdminDashboard();
      await loadPendingStudents();
    } catch (err) {
      window.common.showToast(err.message || "Failed to reject student", "error");
    }
  });
};

window.loadPendingTeachers = loadPendingTeachers;
window.loadPendingStudents = loadPendingStudents;

async function loadAdminAnnouncements() {
  const container = document.getElementById("admin-announcements-container");
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
            📢 Active University Announcements
          </h3>
          <span class="badge badge-info">${list.length} Notice${list.length > 1 ? 's' : ''}</span>
        </div>
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          ${list.slice(0, 3).map(a => `
            <div class="announcement-card priority-${window.common.escapeHTML(a.priority)}" style="margin-bottom: 0; padding: 1rem;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
                <strong style="font-size: 0.95rem; color: #0f172a;">${window.common.escapeHTML(a.title)}</strong>
                <div style="display: flex; gap: 0.5rem; align-items: center;">
                  <span class="badge ${a.priority === 'URGENT' ? 'badge-danger' : (a.priority === 'HIGH' ? 'badge-warning' : 'badge-info')}">${window.common.escapeHTML(a.priority)}</span>
                  <button class="btn btn-danger-outline btn-sm" style="padding: 0.15rem 0.4rem; font-size: 0.7rem;" onclick="handleDeleteAnnouncement('${a.id}')">Delete</button>
                </div>
              </div>
              <p style="font-size: 0.85rem; color: #334155; margin-bottom: 0.5rem; line-height: 1.5;">${window.common.escapeHTML(a.content)}</p>
              <div style="font-size: 0.75rem; color: var(--text-secondary); display: flex; gap: 1rem;">
                <span>📍 Target: ${window.common.escapeHTML(a.targetRole || 'ALL')}</span>
                <span>👤 Author: ${window.common.escapeHTML(a.authorName || 'Administrator')}</span>
                <span>⏱️ ${window.common.formatDate(a.createdAt)}</span>
              </div>
            </div>
          `).join("")}
        </div>
      </div>
    `;
  } catch (e) {
    console.warn("Could not load admin announcements:", e);
  }
}

async function loadAuditLogs() {
  const container = document.getElementById("admin-audit-logs-container");
  if (!container) return;

  try {
    const logs = await window.api.get("/api/admin/audit-logs").catch(() => []);
    if (!Array.isArray(logs) || logs.length === 0) {
      window.common.renderEmptyState(container, "No Audit Records", "No administrative actions have been recorded yet.");
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Action</th>
              <th>Actor Email</th>
              <th>Target Entity</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            ${logs.slice(0, 10).map(l => {
              let actionBadge = "badge-neutral";
              if (l.action && l.action.includes("APPROVED")) actionBadge = "badge-success";
              else if (l.action && l.action.includes("REJECTED")) actionBadge = "badge-danger";
              else if (l.action && l.action.includes("CREATED")) actionBadge = "badge-info";

              return `
                <tr>
                  <td class="text-muted tabular-nums" style="font-size: 0.8rem; white-space: nowrap;">${window.common.formatDate(l.createdAt)}</td>
                  <td><span class="badge ${actionBadge}" style="font-family: var(--font-mono); font-size: 0.7rem;">${window.common.escapeHTML(l.action)}</span></td>
                  <td><strong>${window.common.escapeHTML(l.userEmail || '-')}</strong></td>
                  <td><code style="font-size: 0.75rem; background: #f1f5f9; padding: 0.15rem 0.35rem; border-radius: 4px;">${window.common.escapeHTML(l.entityType)} #${window.common.escapeHTML(l.entityId || '')}</code></td>
                  <td class="text-muted" style="font-size: 0.8rem; max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${window.common.escapeHTML(l.details || '-')}</td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    window.common.renderErrorState(container, err.message, () => loadAuditLogs());
  }
}

function setupAdminAnnouncementModal() {
  const form = document.getElementById("admin-announcement-form");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("adm-ann-title").value.trim();
    const priority = document.getElementById("adm-ann-priority").value;
    const targetRole = document.getElementById("adm-ann-target").value;
    const content = document.getElementById("adm-ann-content").value.trim();

    try {
      const btn = document.getElementById("adm-ann-submit-btn");
      btn.disabled = true;
      btn.innerText = "Publishing...";

      await window.api.post("/api/announcements", {
        title,
        priority,
        targetRole,
        content
      });

      window.common.showToast("University announcement published successfully!", "success");
      window.common.closeModal("admin-announcement-modal");
      form.reset();
      await loadAdminAnnouncements();
    } catch (err) {
      window.common.showToast(err.message || "Failed to publish notice", "error");
    } finally {
      const btn = document.getElementById("adm-ann-submit-btn");
      if (btn) {
        btn.disabled = false;
        btn.innerText = "Publish to University";
      }
    }
  });
}

window.handleDeleteAnnouncement = async function(id) {
  window.common.confirmAction("Are you sure you want to delete this notice?", async () => {
    try {
      await window.api.delete(`/api/announcements/${id}`);
      window.common.showToast("Announcement removed.", "info");
      await loadAdminAnnouncements();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete notice", "error");
    }
  });
};

window.openAdminAnnouncementModal = function() {
  window.common.openModal("admin-announcement-modal");
};

window.loadAuditLogs = loadAuditLogs;
