/**
 * Galgotias LMS
 * Admin User Management Controller
 * Connects to:
 * - GET /api/admin/users (with ?q= search)
 * - PUT /api/admin/teachers/{id}/approve
 * - PUT /api/admin/teachers/{id}/reject
 * - PUT /api/admin/students/{id}/approve
 * - PUT /api/admin/students/{id}/reject
 * - PUT /api/admin/users/{id}/activate
 * - PUT /api/admin/users/{id}/deactivate
 */

let allUsers = [];
let roleFilter = "ALL"; // "ALL" | "ADMIN" | "STUDENT" | "TEACHER" | "PENDING_TEACHERS" | "PENDING_STUDENTS" | "ACTIVE" | "INACTIVE"
let searchQuery = "";

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "users");

  setupFilters();
  setupSearch();
  await loadUsers();
});

function setupFilters() {
  const fAll = document.getElementById("filter-all");
  const fAdm = document.getElementById("filter-admins");
  const fStu = document.getElementById("filter-students");
  const fTea = document.getElementById("filter-teachers");
  const fPenTea = document.getElementById("filter-pending-teachers") || document.getElementById("filter-pending");
  const fPenStu = document.getElementById("filter-pending-students");
  const fAct = document.getElementById("filter-active");
  const fInact = document.getElementById("filter-inactive");

  const resetButtons = () => {
    [fAll, fAdm, fStu, fTea, fPenTea, fPenStu, fAct, fInact].forEach(b => {
      if (b) b.className = "btn btn-sm btn-secondary";
    });
  };

  fAll?.addEventListener("click", () => {
    roleFilter = "ALL";
    resetButtons();
    fAll.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fAdm?.addEventListener("click", () => {
    roleFilter = "ADMIN";
    resetButtons();
    fAdm.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fStu?.addEventListener("click", () => {
    roleFilter = "STUDENT";
    resetButtons();
    fStu.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fTea?.addEventListener("click", () => {
    roleFilter = "TEACHER";
    resetButtons();
    fTea.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fPenTea?.addEventListener("click", () => {
    roleFilter = "PENDING_TEACHERS";
    resetButtons();
    fPenTea.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fPenStu?.addEventListener("click", () => {
    roleFilter = "PENDING_STUDENTS";
    resetButtons();
    fPenStu.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fAct?.addEventListener("click", () => {
    roleFilter = "ACTIVE";
    resetButtons();
    fAct.className = "btn btn-sm btn-primary";
    renderUsers();
  });

  fInact?.addEventListener("click", () => {
    roleFilter = "INACTIVE";
    resetButtons();
    fInact.className = "btn btn-sm btn-primary";
    renderUsers();
  });
}

function setupSearch() {
  const searchInput = document.getElementById("users-search-input");
  if (!searchInput) return;

  let debounceTimer;
  searchInput.addEventListener("input", (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      searchQuery = (e.target.value || "").trim().toLowerCase();
      renderUsers();
    }, 200);
  });
}

async function loadUsers(query = "") {
  const container = document.getElementById("users-table-container");
  const countBadge = document.getElementById("users-count-badge");
  const errorArea = document.getElementById("users-error-area");
  if (errorArea) errorArea.classList.add("hidden");

  try {
    const url = query ? `/api/admin/users?q=${encodeURIComponent(query)}` : "/api/admin/users";
    let res = await window.api.get(url).catch(async () => {
      return await window.api.get("/api/users");
    });

    allUsers = Array.isArray(res) ? res : (res.users || []);
    if (countBadge) {
      countBadge.innerText = `${allUsers.length} User${allUsers.length === 1 ? '' : 's'}`;
    }

    if (allUsers.length === 0) {
      window.common.renderEmptyState(container, "No Users Registered", "There are currently no users in the database.");
      return;
    }

    renderUsers();
  } catch (error) {
    if (errorArea) {
      errorArea.innerText = error.message;
      errorArea.classList.remove("hidden");
    }
    window.common.renderErrorState(container, error.message, () => loadUsers());
  }
}

function renderUsers() {
  const container = document.getElementById("users-table-container");
  if (!container) return;

  const filtered = allUsers.filter(u => {
    const role = (u.role || "").toUpperCase();
    const approval = (u.approvalStatus || "APPROVED").toUpperCase();
    const isActive = u.status === "ACTIVE" || u.active === true || u.enabled !== false;

    // 1. Filter checks
    if (roleFilter === "PENDING_TEACHERS") {
      if (!(role === "TEACHER" && approval === "PENDING")) return false;
    } else if (roleFilter === "PENDING_STUDENTS") {
      if (!(role === "STUDENT" && approval === "PENDING")) return false;
    } else if (roleFilter === "ACTIVE") {
      if (!isActive) return false;
    } else if (roleFilter === "INACTIVE") {
      if (isActive) return false;
    } else if (roleFilter !== "ALL") {
      if (role !== roleFilter) return false;
    }

    // 2. Search Query Filter
    if (searchQuery) {
      const name = (u.name || "").toLowerCase();
      const email = (u.email || "").toLowerCase();
      const profId = (u.professionalId || "").toLowerCase();
      const empId = (u.employeeId || "").toLowerCase();
      const enroll = (u.enrollmentNumber || "").toLowerCase();
      const dept = (u.department || "").toLowerCase();

      const matches = name.includes(searchQuery) ||
                      email.includes(searchQuery) ||
                      profId.includes(searchQuery) ||
                      empId.includes(searchQuery) ||
                      enroll.includes(searchQuery) ||
                      dept.includes(searchQuery);

      if (!matches) return false;
    }

    return true;
  });

  if (filtered.length === 0) {
    let label = "Users";
    if (roleFilter === "PENDING_TEACHERS") label = "Pending Teachers";
    else if (roleFilter === "PENDING_STUDENTS") label = "Pending Students";
    else if (roleFilter === "ACTIVE") label = "Active Users";
    else if (roleFilter === "INACTIVE") label = "Inactive Users";
    else if (roleFilter !== "ALL") label = `${roleFilter}s`;

    window.common.renderEmptyState(container, `No ${label} Found`, searchQuery ? `No matches found for "${searchQuery}".` : `No users found matching the selected filter.`);
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>User</th>
            <th>Email & Professional ID</th>
            <th>Role</th>
            <th>Approval</th>
            <th>Status</th>
            <th>Registered Date</th>
            <th style="text-align: right;">Account Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map(u => {
            const id = u.id || u.userId;
            const name = u.name || u.fullName || "User";
            const email = u.email || "-";
            const role = (u.role || "STUDENT").toUpperCase();
            const profId = u.professionalId;
            const empId = u.employeeId;
            const enroll = u.enrollmentNumber;
            const approval = (u.approvalStatus || "APPROVED").toUpperCase();
            const isActive = u.status === "ACTIVE" || u.active === true || u.enabled !== false;
            const date = u.createdAt || u.createdDate;
            const isPrimaryAdmin = (id === 1 || email === "admin@lms.com");

            return `
              <tr>
                <td>
                  <strong>${window.common.escapeHTML(name)}</strong>
                </td>
                <td>
                  <div>${window.common.escapeHTML(email)}</div>
                  ${profId ? `
                    <div style="margin-top: 0.25rem;">
                      <code style="font-size: 0.75rem; background: rgba(30, 58, 138, 0.08); color: var(--primary); padding: 0.12rem 0.4rem; border-radius: 4px; font-weight: 700; font-family: monospace;">
                        ${window.common.escapeHTML(profId)}
                      </code>
                    </div>
                  ` : ''}
                  ${empId && !profId ? `
                    <div style="margin-top: 0.2rem;">
                      <code style="font-size: 0.75rem; background: rgba(37, 99, 235, 0.08); color: var(--primary); padding: 0.1rem 0.35rem; border-radius: 4px;">
                        EMP: ${window.common.escapeHTML(empId)}
                      </code>
                    </div>
                  ` : ''}
                  ${enroll && !profId ? `
                    <div style="margin-top: 0.2rem;">
                      <code style="font-size: 0.75rem; background: rgba(100, 116, 139, 0.1); color: #475569; padding: 0.1rem 0.35rem; border-radius: 4px;">
                        ENR: ${window.common.escapeHTML(enroll)}
                      </code>
                    </div>
                  ` : ''}
                </td>
                <td>
                  <span class="badge ${role === 'ADMIN' ? 'badge-danger' : role === 'TEACHER' ? 'badge-primary' : 'badge-info'}">
                    ${role}
                  </span>
                </td>
                <td>
                  <span class="badge ${approval === 'APPROVED' ? 'badge-success' : approval === 'PENDING' ? 'badge-warning' : 'badge-danger'}">
                    ${approval === 'APPROVED' ? 'Approved' : approval === 'PENDING' ? 'Pending' : 'Rejected'}
                  </span>
                </td>
                <td>
                  <span class="badge ${isActive ? 'badge-success' : 'badge-neutral'}">
                    ${isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                <td style="text-align: right;">
                  <div class="flex items-center justify-end gap-2 flex-wrap">
                    <button class="btn btn-secondary btn-sm" onclick="openUserProfileModal('${id}')" title="View Full Profile">
                      👤 View
                    </button>
                    ${isPrimaryAdmin ? `
                      <span class="badge" style="background: #e0e7ff; color: #4338ca; font-size: 0.75rem;">Primary Admin</span>
                    ` : role === 'ADMIN' ? `
                      <span class="text-muted" style="font-size: 0.8rem;">Protected</span>
                    ` : (role === 'TEACHER' && approval === 'PENDING') ? `
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none; padding: 0.25rem 0.6rem;"
                        onclick="handleApproveTeacher('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                      <button
                        class="btn btn-danger-outline btn-sm"
                        style="padding: 0.25rem 0.6rem;"
                        onclick="handleRejectTeacher('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✕ Reject
                      </button>
                    ` : (role === 'STUDENT' && approval === 'PENDING') ? `
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none; padding: 0.25rem 0.6rem;"
                        onclick="handleApproveStudent('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                      <button
                        class="btn btn-danger-outline btn-sm"
                        style="padding: 0.25rem 0.6rem;"
                        onclick="handleRejectStudent('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✕ Reject
                      </button>
                    ` : (role === 'TEACHER' && approval === 'REJECTED') ? `
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none; padding: 0.25rem 0.6rem;"
                        onclick="handleApproveTeacher('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                    ` : (role === 'STUDENT' && approval === 'REJECTED') ? `
                      <button
                        class="btn btn-sm"
                        style="background: #16a34a; color: #ffffff; border: none; padding: 0.25rem 0.6rem;"
                        onclick="handleApproveStudent('${id}', '${window.common.escapeHTML(name)}')"
                      >
                        ✓ Approve
                      </button>
                    ` : isActive ? `
                      <button class="btn btn-danger-outline btn-sm" onclick="handleToggleStatus('${id}', false, '${window.common.escapeHTML(name)}')">
                        Deactivate
                      </button>
                    ` : `
                      <button class="btn btn-secondary btn-sm" style="color: #16a34a;" onclick="handleToggleStatus('${id}', true, '${window.common.escapeHTML(name)}')">
                        Activate
                      </button>
                    `}
                  </div>
                </td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

window.openUserProfileModal = function(userId) {
  const u = allUsers.find(item => String(item.id || item.userId) === String(userId));
  if (!u) return;

  const body = document.getElementById("user-profile-modal-body");
  const footer = document.getElementById("user-profile-modal-footer");
  const name = u.name || u.fullName || "User";
  const email = u.email || "-";
  const role = (u.role || "STUDENT").toUpperCase();
  const profId = u.professionalId || "Pending assignment on approval";
  const empId = u.employeeId;
  const enroll = u.enrollmentNumber;
  const dept = u.department || "General Academics";
  const approval = (u.approvalStatus || "APPROVED").toUpperCase();
  const isActive = u.status === "ACTIVE" || u.active === true || u.enabled !== false;
  const date = u.createdAt || u.createdDate;
  const isPrimaryAdmin = (u.id === 1 || email === "admin@lms.com");
  const initial = (name.charAt(0) || "U").toUpperCase();

  body.innerHTML = `
    <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--border-color);">
      <div class="user-avatar-circle" style="width: 56px; height: 56px; font-size: 1.4rem; background: var(--primary); color: #fff;">
        ${window.common.escapeHTML(initial)}
      </div>
      <div>
        <h3 style="font-size: 1.25rem; font-weight: 700; color: var(--dark); margin: 0;">${window.common.escapeHTML(name)}</h3>
        <div style="display: flex; align-items: center; gap: 0.5rem; margin-top: 0.35rem; flex-wrap: wrap;">
          <span class="badge ${role === 'ADMIN' ? 'badge-danger' : role === 'TEACHER' ? 'badge-primary' : 'badge-info'}">${role}</span>
          <span class="badge ${approval === 'APPROVED' ? 'badge-success' : approval === 'PENDING' ? 'badge-warning' : 'badge-danger'}">${approval}</span>
          <span class="badge ${isActive ? 'badge-success' : 'badge-neutral'}">${isActive ? 'Active' : 'Inactive'}</span>
        </div>
      </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; font-size: 0.875rem;">
      <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
        <span class="text-muted" style="display: block; font-size: 0.75rem;">Professional ID</span>
        <strong style="color: var(--primary); font-family: monospace; font-size: 0.95rem;">${window.common.escapeHTML(profId)}</strong>
      </div>

      <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
        <span class="text-muted" style="display: block; font-size: 0.75rem;">University Email</span>
        <strong>${window.common.escapeHTML(email)}</strong>
      </div>

      ${empId ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Employee ID</span>
          <strong>${window.common.escapeHTML(empId)}</strong>
        </div>
      ` : ''}

      ${enroll ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Enrollment Number</span>
          <strong>${window.common.escapeHTML(enroll)}</strong>
        </div>
      ` : ''}

      <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
        <span class="text-muted" style="display: block; font-size: 0.75rem;">Department / School</span>
        <strong>${window.common.escapeHTML(dept)}</strong>
      </div>

      ${u.phone ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Phone Number</span>
          <strong>${window.common.escapeHTML(u.phone)}</strong>
        </div>
      ` : ''}

      ${u.designation ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Designation</span>
          <strong>${window.common.escapeHTML(u.designation)}</strong>
        </div>
      ` : ''}

      ${u.qualification ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Qualification</span>
          <strong>${window.common.escapeHTML(u.qualification)}</strong>
        </div>
      ` : ''}

      ${u.specialization ? `
        <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <span class="text-muted" style="display: block; font-size: 0.75rem;">Specialization</span>
          <strong>${window.common.escapeHTML(u.specialization)}</strong>
        </div>
      ` : ''}

      <div style="background: var(--bg-alt); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
        <span class="text-muted" style="display: block; font-size: 0.75rem;">Registration Date</span>
        <strong class="tabular-nums">${window.common.formatDate(date)}</strong>
      </div>
    </div>
  `;

  let actionsHtml = `<button type="button" class="btn btn-secondary" onclick="window.common.closeModal('user-profile-modal')">Close</button>`;

  if (!isPrimaryAdmin && role !== 'ADMIN') {
    if (approval === 'PENDING') {
      const approveFn = role === 'TEACHER' ? `handleApproveTeacher('${u.id}', '${window.common.escapeHTML(name)}')` : `handleApproveStudent('${u.id}', '${window.common.escapeHTML(name)}')`;
      const rejectFn = role === 'TEACHER' ? `handleRejectTeacher('${u.id}', '${window.common.escapeHTML(name)}')` : `handleRejectStudent('${u.id}', '${window.common.escapeHTML(name)}')`;
      actionsHtml += `
        <button class="btn btn-danger-outline btn-sm" onclick="window.common.closeModal('user-profile-modal'); ${rejectFn}">✕ Reject</button>
        <button class="btn btn-primary btn-sm" style="background: #16a34a; border-color: #16a34a;" onclick="window.common.closeModal('user-profile-modal'); ${approveFn}">✓ Approve Registration</button>
      `;
    } else {
      if (isActive) {
        actionsHtml += `
          <button class="btn btn-danger-outline btn-sm" onclick="window.common.closeModal('user-profile-modal'); handleToggleStatus('${u.id}', false, '${window.common.escapeHTML(name)}')">
            Deactivate Account
          </button>
        `;
      } else {
        actionsHtml += `
          <button class="btn btn-primary btn-sm" style="background: #16a34a; border-color: #16a34a;" onclick="window.common.closeModal('user-profile-modal'); handleToggleStatus('${u.id}', true, '${window.common.escapeHTML(name)}')">
            Activate Account
          </button>
        `;
      }
    }
  }

  footer.innerHTML = actionsHtml;
  window.common.openModal("user-profile-modal");
};

window.handleApproveTeacher = async function(userId, name) {
  window.common.confirmAction(`Are you sure you want to approve faculty account for ${name}? A unique Teacher ID (GUL-TEA-2026-XXX) will be assigned.`, async () => {
    try {
      await window.api.put(`/api/admin/teachers/${userId}/approve`, {});
      window.common.showToast(`Faculty member ${name} approved successfully!`, "success");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || "Failed to approve teacher", "error");
    }
  });
};

window.handleRejectTeacher = async function(userId, name) {
  window.common.confirmAction(`Are you sure you want to reject faculty registration for ${name}?`, async () => {
    try {
      await window.api.put(`/api/admin/teachers/${userId}/reject`, {});
      window.common.showToast(`Faculty registration for ${name} rejected.`, "info");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || "Failed to reject teacher", "error");
    }
  });
};

window.handleApproveStudent = async function(userId, name) {
  window.common.confirmAction(`Are you sure you want to approve student account for ${name}? A unique Student ID (GUL-STU-2026-XXX) will be assigned.`, async () => {
    try {
      await window.api.put(`/api/admin/students/${userId}/approve`, {});
      window.common.showToast(`Student ${name} approved and activated successfully!`, "success");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || "Failed to approve student", "error");
    }
  });
};

window.handleRejectStudent = async function(userId, name) {
  window.common.confirmAction(`Are you sure you want to reject student registration for ${name}?`, async () => {
    try {
      await window.api.put(`/api/admin/students/${userId}/reject`, {});
      window.common.showToast(`Student registration for ${name} rejected.`, "info");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || "Failed to reject student", "error");
    }
  });
};

window.handleToggleStatus = async function(userId, enable, name) {
  const actionName = enable ? "activate" : "deactivate";
  const confirmMsg = enable
    ? `Are you sure you want to activate the account for ${name}?`
    : `Are you sure you want to deactivate the account for ${name}? All active sessions and JWT tokens will be immediately invalidated.`;

  window.common.confirmAction(confirmMsg, async () => {
    try {
      await window.api.put(`/api/admin/users/${userId}/${actionName}`, {}).catch(async () => {
        return await window.api.put(`/api/users/${userId}/${actionName}`, {});
      });

      window.common.showToast(`User ${name} has been ${enable ? 'activated' : 'deactivated'} successfully!`, "success");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || `Failed to ${actionName} user`, "error");
    }
  });
};
