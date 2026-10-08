/**
 * Smart Learning Management System
 * Admin User Management Controller
 * Connects to:
 * GET /api/users (or /api/admin/users)
 * PUT /api/users/{id}/activate
 * PUT /api/users/{id}/deactivate
 */

let allUsers = [];
let roleFilter = "ALL"; // "ALL" | "STUDENT" | "TEACHER"

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "users");

  setupFilters();
  await loadUsers();
});

function setupFilters() {
  const fAll = document.getElementById("filter-all");
  const fStu = document.getElementById("filter-students");
  const fTea = document.getElementById("filter-teachers");

  fAll.addEventListener("click", () => {
    roleFilter = "ALL";
    fAll.className = "btn btn-sm btn-primary";
    fStu.className = fTea.className = "btn btn-sm btn-secondary";
    renderUsers();
  });

  fStu.addEventListener("click", () => {
    roleFilter = "STUDENT";
    fStu.className = "btn btn-sm btn-primary";
    fAll.className = fTea.className = "btn btn-sm btn-secondary";
    renderUsers();
  });

  fTea.addEventListener("click", () => {
    roleFilter = "TEACHER";
    fTea.className = "btn btn-sm btn-primary";
    fAll.className = fStu.className = "btn btn-sm btn-secondary";
    renderUsers();
  });
}

async function loadUsers() {
  const container = document.getElementById("users-table-container");
  const countBadge = document.getElementById("users-count-badge");
  const errorArea = document.getElementById("users-error-area");
  errorArea.classList.add("hidden");

  try {
    let res = await window.api.get("/api/users").catch(async () => {
      return await window.api.get("/api/admin/users");
    });

    allUsers = Array.isArray(res) ? res : (res.users || []);
    countBadge.innerText = `${allUsers.length} User${allUsers.length === 1 ? '' : 's'}`;

    if (allUsers.length === 0) {
      window.common.renderEmptyState(container, "No Users Registered", "There are currently no users in the database.");
      return;
    }

    renderUsers();
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadUsers());
  }
}

function renderUsers() {
  const container = document.getElementById("users-table-container");
  const filtered = allUsers.filter(u => {
    if (roleFilter === "ALL") return true;
    return (u.role || "").toUpperCase() === roleFilter;
  });

  if (filtered.length === 0) {
    window.common.renderEmptyState(container, `No ${roleFilter}s Found`, `No users found matching the ${roleFilter} role.`);
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Created Date</th>
            <th>Account Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map(u => {
            const id = u.id || u.userId;
            const name = u.name || u.fullName || "User";
            const email = u.email || "-";
            const role = (u.role || "STUDENT").toUpperCase();
            const isActive = u.status === "ACTIVE" || u.active === true || u.enabled !== false;
            const date = u.createdAt || u.createdDate;

            return `
              <tr>
                <td><strong>${window.common.escapeHTML(name)}</strong></td>
                <td class="text-muted">${window.common.escapeHTML(email)}</td>
                <td>
                  <span class="badge ${role === 'ADMIN' ? 'badge-danger' : role === 'TEACHER' ? 'badge-primary' : 'badge-info'}">
                    ${role}
                  </span>
                </td>
                <td>
                  <span class="badge ${isActive ? 'badge-success' : 'badge-neutral'}">
                    ${isActive ? 'Active' : 'Deactivated'}
                  </span>
                </td>
                <td class="text-muted tabular-nums">${window.common.formatDate(date)}</td>
                <td>
                  ${role === 'ADMIN' ? `
                    <span class="text-muted" style="font-size: 0.8rem;">Protected</span>
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

window.handleToggleStatus = async function(userId, enable, name) {
  const actionName = enable ? "activate" : "deactivate";
  window.common.confirmAction(`Are you sure you want to ${actionName} the account for ${name}?`, async () => {
    try {
      // Try PUT /api/users/{id}/activate or /api/admin/users/{id}/activate
      await window.api.put(`/api/users/${userId}/${actionName}`, {}).catch(async () => {
        return await window.api.put(`/api/admin/users/${userId}/${actionName}`, {});
      });

      window.common.showToast(`User ${name} has been ${enable ? 'activated' : 'deactivated'} successfully!`, "success");
      await loadUsers();
    } catch (error) {
      window.common.showToast(error.message || `Failed to ${actionName} user`, "error");
    }
  });
};
