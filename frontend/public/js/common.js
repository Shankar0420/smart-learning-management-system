/**
 * Smart Learning Management System
 * Common Utilities, Layout Injector & UI Helpers
 */

/**
 * Toast notification dispatcher
 */
function showToast(message, type = "info", duration = 4000) {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${escapeHTML(message)}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    toast.style.transition = "all 0.2s ease";
    setTimeout(() => toast.remove(), 250);
  }, duration);
}

/**
 * Helper to escape HTML to prevent XSS
 */
function escapeHTML(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

/**
 * Clean Date Formatter
 */
function formatDate(dateInput) {
  if (!dateInput) return "N/A";
  try {
    const d = new Date(dateInput);
    if (isNaN(d.getTime())) return String(dateInput);
    return d.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  } catch (e) {
    return String(dateInput);
  }
}

/**
 * UI State: Loading Spinner
 */
function renderLoading(container, text = "Loading data...") {
  if (!container) return;
  container.innerHTML = `
    <div class="loading-container">
      <div class="spinner-primary"></div>
      <p>${escapeHTML(text)}</p>
    </div>
  `;
}

/**
 * UI State: Empty State
 */
function renderEmptyState(container, title = "No data found", description = "There are no records to display.", actionHtml = "") {
  if (!container) return;
  container.innerHTML = `
    <div class="empty-state">
      <div class="empty-state-icon">📋</div>
      <h3 class="empty-state-title">${escapeHTML(title)}</h3>
      <p class="empty-state-desc">${escapeHTML(description)}</p>
      ${actionHtml ? `<div>${actionHtml}</div>` : ""}
    </div>
  `;
}

/**
 * UI State: Error Message with Retry
 */
function renderErrorState(container, message = "Unable to load data", retryFn = null) {
  if (!container) return;
  const retryBtnId = `retry-btn-${Date.now()}`;
  container.innerHTML = `
    <div class="empty-state">
      <div class="empty-state-icon" style="color: var(--danger); background-color: var(--danger-bg);">⚠️</div>
      <h3 class="empty-state-title">Error Loading Data</h3>
      <p class="empty-state-desc" style="color: #991b1b;">${escapeHTML(message)}</p>
      ${retryFn ? `<button id="${retryBtnId}" class="btn btn-secondary btn-sm">Try Again</button>` : ""}
    </div>
  `;

  if (retryFn) {
    document.getElementById(retryBtnId)?.addEventListener("click", retryFn);
  }
}

/**
 * Modal Open & Close Utilities
 */
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("active");
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("active");
  }
}

/**
 * Browser Confirmation Wrapper
 */
function confirmAction(message, onConfirm) {
  if (window.confirm(message)) {
    onConfirm();
  }
}

/**
 * Initialize Common Dashboard Layout (Sidebar, Navigation, Topbar)
 */
function initDashboardLayout(role, activePageKey) {
  const user = window.auth.getCurrentUser();
  const userName = user ? user.name || user.email : "User";
  const userRole = (user ? user.role : role || "STUDENT").toUpperCase();
  const initial = userName.charAt(0).toUpperCase();

  // Navigation Links by Role
  const navConfigs = {
    STUDENT: [
      { key: "dashboard", label: "Dashboard", href: "/student/dashboard.html", icon: "📊" },
      { key: "courses", label: "My Courses", href: "/student/courses.html", icon: "📚" },
      { key: "assignments", label: "Assignments", href: "/student/assignments.html", icon: "📝" },
      { key: "grades", label: "Grades", href: "/student/grades.html", icon: "🏅" },
      { key: "profile", label: "Profile", href: "/student/profile.html", icon: "👤" }
    ],
    TEACHER: [
      { key: "dashboard", label: "Dashboard", href: "/teacher/dashboard.html", icon: "📊" },
      { key: "courses", label: "My Courses", href: "/teacher/courses.html", icon: "📚" },
      { key: "assignments", label: "Assignments", href: "/teacher/assignment-management.html", icon: "📝" },
      { key: "submissions", label: "Submissions", href: "/teacher/submissions.html", icon: "📥" },
      { key: "profile", label: "Profile", href: "/teacher/profile.html", icon: "👤" }
    ],
    ADMIN: [
      { key: "dashboard", label: "Dashboard", href: "/admin/dashboard.html", icon: "📊" },
      { key: "users", label: "Users", href: "/admin/users.html", icon: "👥" },
      { key: "courses", label: "Courses", href: "/admin/courses.html", icon: "📚" },
      { key: "profile", label: "Profile", href: "/admin/profile.html", icon: "👤" }
    ]
  };

  const currentNav = navConfigs[userRole] || navConfigs.STUDENT;

  // Populate Sidebar
  const sidebarContainer = document.getElementById("sidebar-container");
  if (sidebarContainer) {
    const navLinksHtml = currentNav.map(item => `
      <a href="${item.href}" class="nav-link ${item.key === activePageKey ? "active" : ""}">
        <span class="nav-icon">${item.icon}</span>
        <span>${item.label}</span>
      </a>
    `).join("");

    sidebarContainer.innerHTML = `
      <aside class="dashboard-sidebar" id="dashboard-sidebar">
        <div class="sidebar-brand">
          <div class="brand-icon">GU</div>
          <div class="brand-text">
            <h2>Galgotias LMS</h2>
            <span>Smart LMS Portal</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-section-title">Main Navigation</div>
          ${navLinksHtml}
        </nav>

        <div class="sidebar-footer">
          <div class="user-mini-profile">
            <div class="user-avatar-circle">${escapeHTML(initial)}</div>
            <div class="user-info-text">
              <div class="user-name" title="${escapeHTML(userName)}">${escapeHTML(userName)}</div>
              <div class="user-role-badge">${escapeHTML(userRole)}</div>
            </div>
            <button id="sidebar-logout-btn" class="btn btn-secondary btn-sm" title="Log Out" style="padding: 0.25rem 0.5rem;">
              🚪
            </button>
          </div>
        </div>
      </aside>
      <div class="sidebar-overlay" id="sidebar-overlay"></div>
    `;

    document.getElementById("sidebar-logout-btn")?.addEventListener("click", () => {
      window.auth.logout();
    });
  }

  // Populate Top Header
  const headerContainer = document.getElementById("header-container");
  if (headerContainer) {
    const pageTitle = document.getElementById("page-title-text")?.innerText || "Dashboard";
    const currentBaseUrl = window.APP_CONFIG.getApiBaseUrl();

    headerContainer.innerHTML = `
      <header class="dashboard-header">
        <div class="header-left">
          <button class="mobile-menu-btn" id="mobile-toggle-btn" aria-label="Toggle Navigation">
            ☰
          </button>
          <div class="header-title-wrap">
            <h1 id="header-page-title">${escapeHTML(pageTitle)}</h1>
          </div>
        </div>

        <div class="header-right">
          <button class="api-badge" id="api-config-btn" title="Click to view or change backend URL">
            <span class="status-dot connected" id="api-status-dot"></span>
            <span>API: ${escapeHTML(currentBaseUrl)}</span>
          </button>
          <button class="btn btn-secondary btn-sm" id="top-logout-btn">
            Log Out
          </button>
        </div>
      </header>
    `;

    document.getElementById("top-logout-btn")?.addEventListener("click", () => {
      window.auth.logout();
    });

    document.getElementById("api-config-btn")?.addEventListener("click", () => {
      openApiConfigModal();
    });
  }

  // Mobile Drawer Toggle
  const mobileBtn = document.getElementById("mobile-toggle-btn");
  const sidebar = document.getElementById("dashboard-sidebar");
  const overlay = document.getElementById("sidebar-overlay");

  if (mobileBtn && sidebar && overlay) {
    mobileBtn.addEventListener("click", () => {
      sidebar.classList.toggle("mobile-open");
      overlay.classList.toggle("active");
    });

    overlay.addEventListener("click", () => {
      sidebar.classList.remove("mobile-open");
      overlay.classList.remove("active");
    });
  }

  // Inject Backend API Config Modal once into DOM
  injectApiConfigModal();
}

/**
 * Injects API URL Configuration Modal for easy testing & backend switching
 */
function injectApiConfigModal() {
  if (document.getElementById("api-config-modal")) return;

  const modalHtml = `
    <div class="modal-overlay" id="api-config-modal">
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">Backend API Configuration</h3>
          <button class="modal-close" onclick="closeModal('api-config-modal')">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label" for="cfg-backend-url">Spring Boot Backend URL</label>
            <input type="text" id="cfg-backend-url" class="form-input" value="${window.APP_CONFIG.getApiBaseUrl()}" placeholder="http://localhost:8080" />
            <p class="form-help">Default: http://localhost:8080. If your Spring Boot server is running on a different port or host, update it here.</p>
          </div>
          <div id="cfg-test-result" style="margin-top: 0.5rem;"></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" id="cfg-test-btn">Test Connection</button>
          <button class="btn btn-primary" id="cfg-save-btn">Save & Reload</button>
        </div>
      </div>
    </div>
  `;

  document.body.insertAdjacentHTML("beforeend", modalHtml);

  document.getElementById("cfg-test-btn")?.addEventListener("click", async () => {
    const testResult = document.getElementById("cfg-test-result");
    testResult.innerHTML = '<span class="text-muted">Testing backend connection...</span>';
    const testUrl = document.getElementById("cfg-backend-url").value.trim();
    try {
      const resp = await fetch(`${testUrl}/api/courses`, { method: "GET" });
      testResult.innerHTML = `<span class="badge badge-success">✓ Backend reachable (${resp.status})</span>`;
    } catch (e) {
      testResult.innerHTML = `<span class="badge badge-danger">✗ Cannot reach backend at ${testUrl}: ${escapeHTML(e.message)}</span>`;
    }
  });

  document.getElementById("cfg-save-btn")?.addEventListener("click", () => {
    const newUrl = document.getElementById("cfg-backend-url").value.trim();
    window.APP_CONFIG.setApiBaseUrl(newUrl);
    showToast("Backend URL updated successfully. Reloading...", "success");
    setTimeout(() => window.location.reload(), 600);
  });
}

function openApiConfigModal() {
  openModal("api-config-modal");
}

// Global exports
window.common = {
  showToast,
  escapeHTML,
  formatDate,
  renderLoading,
  renderEmptyState,
  renderErrorState,
  openModal,
  closeModal,
  confirmAction,
  initDashboardLayout,
  openApiConfigModal
};
