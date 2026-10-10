/**
 * Smart Learning Management System
 * Admin User Management Controller
 * Connects to:
 * - GET /api/admin/users
 * - PUT /api/admin/teachers/{id}/approve
 * - PUT /api/admin/teachers/{id}/reject
 * - PUT /api/admin/users/{id}/activate
 * - PUT /api/admin/users/{id}/deactivate
 */

let allUsers = [];
let roleFilter = "ALL"; // "ALL" | "STUDENT" | "TEACHER" | "PENDING"

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "users");

  setupFilters();
  await loadUsers();
});

function setupFilters() {
  const fAll = document.getElementById("filter-all");
  const fAdm = document.getElementById("filter-admins");
  const fStu = document.getElementById("filter-students");
  const fTea = document.getElementById("filter-teachers");
  const fPen = document.getElementById("filter-pending");

  const resetButtons = () => {
    [fAll, fAdm, fStu, fTea, fPen].forEach(b => {
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

  fPen?.addEventListener("click", () => {
    roleFilter = "PENDING";
    resetButtons();
    fPen.className = "btn btn-sm btn-primary";
    renderUsers();
  });
}

async function loadUsers() {
  const container = document.getElementById("users-table-container");
  const countBadge = document.getElementById("users-count-badge");
  const errorArea = document.getElementById("users-error-area");
  if (errorArea) errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/admin/users").catch(async () => {
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
    if (roleFilter === "ALL") return true;
    if (roleFilter === "PENDING") {
      return (u.role || "").toUpperCase() === "TEACHER" && (u.approvalStatus || "").toUpperCase() === "PENDING";
    }
    return (u.role || "").toUpperCase() === roleFilter;
  });

  if (filtered.length === 0) {
    const label = roleFilter === "PENDING" ? "Pending Teachers" : `${roleFilter}s`;
    window.common.renderEmptyState(container, `No ${label} Found`, `No users found matching the selected filter.`);
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>User</th>
            <th>Email & Identifier</th>
            <th>Role</th>
            <th>Approval</th>
            <th>Status</th>
            <th>Registered Date</th>
            <th>Account Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map(u => {
            const id = u.id || u.userId;
            const name = u.name || u.fullName || "User";
            const email = u.email || "-";
            const role = (u.role || "STUDENT").toUpperCase();
            const empId = u.employeeId;
            const approval = (u.approvalStatus || "APPROVED").toUpperCase();
            const isActive = u.status === "ACTIVE" || u.active === true || u.enabled !== false;
            const date = u.createdAt || u.createdDate;

            return `
              <tr>
                <td>
                  <strong>${window.common.escapeHTML(name)}</strong>
                </td>
                <td>
                  <div>${window.common.escapeHTML(email)}</div>
                  ${empId ? `
                    <div style="margin-top: 0.2rem;">
                      <code style="font-size: 0.75rem; background: rgba(37, 99, 235, 0.08); color: var(--primary); padding: 0.1rem 0.35rem; border-radius: 4px;">
                        ID: ${window.common.escapeHTML(empId)}
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
                <td>
                  ${role === 'ADMIN' ? `
                    <span class="text-muted" style="font-size: 0.8rem;">Protected</span>
                  ` : role === 'TEACHER' && approval === 'PENDING' ? `
                    <div class="flex gap-2">
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
                    </div>
                  ` : role === 'TEACHER' && approval === 'REJECTED' ? `
                    <button
                      class="btn btn-sm"
                      style="background: #16a34a; color: #ffffff; border: none; padding: 0.25rem 0.6rem;"
                      onclick="handleApproveTeacher('${id}', '${window.common.escapeHTML(name)}')"
                    >
                      ✓ Approve
                    </button>
                  ` : isActive ? `
                    <button class="btn btn-danger-outline btn-sm" onclick="handleToggleStatus('${id}', false, '${window.common.escapeHTML(name)}')">
                      Deactivate
                    </button>
                  ` : `
                    <button class="btn btn-secondary btn-sm" onclick="handleToggleStatus('${id}', true, '${window.common.escapeHTML(name)}')">
                      Activate
                    </button>
                  `}
                </td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

window.handleApproveTeacher = async function(userId, name) {
  window.common.confirmAction(`Are you sure you want to approve faculty account for ${name}?`, async () => {
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

window.handleToggleStatus = async function(userId, enable, name) {
  const actionName = enable ? "activate" : "deactivate";
  window.common.confirmAction(`Are you sure you want to ${actionName} the account for ${name}?`, async () => {
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
