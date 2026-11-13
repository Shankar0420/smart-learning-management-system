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
      { key: "study-materials", label: "Study Materials", href: "/student/study-materials.html", icon: "📑" },
      { key: "quiz", label: "Quizzes & Tests", href: "/student/quiz.html", icon: "⏱️" },
      { key: "assignments", label: "Assignments", href: "/student/assignments.html", icon: "📝" },
      { key: "grades", label: "Grades & Certificates", href: "/student/grades.html", icon: "🏅" },
      { key: "gul-buddy", label: "GUL Buddy AI", href: "javascript:window.common.openGulBuddy()", icon: "✨" },
      { key: "profile", label: "Profile", href: "/student/profile.html", icon: "👤" }
    ],
    TEACHER: [
      { key: "dashboard", label: "Dashboard", href: "/teacher/dashboard.html", icon: "📊" },
      { key: "courses", label: "My Courses", href: "/teacher/courses.html", icon: "📚" },
      { key: "quizzes", label: "Quiz Bank & AI", href: "/teacher/quiz-management.html", icon: "⏱️" },
      { key: "assignments", label: "Assignments", href: "/teacher/assignment-management.html", icon: "📝" },
      { key: "submissions", label: "Submissions", href: "/teacher/submissions.html", icon: "📥" },
      { key: "gul-buddy", label: "GUL Buddy AI", href: "javascript:window.common.openGulBuddy()", icon: "✨" },
      { key: "profile", label: "Faculty Profile & Docs", href: "/teacher/profile.html", icon: "👤" }
    ],
    ADMIN: [
      { key: "dashboard", label: "Dashboard", href: "/admin/dashboard.html", icon: "📊" },
      { key: "users", label: "Users & Faculty", href: "/admin/users.html", icon: "👥" },
      { key: "courses", label: "Course Allocation", href: "/admin/courses.html", icon: "📚" },
      { key: "gul-buddy", label: "GUL Buddy AI", href: "javascript:window.common.openGulBuddy()", icon: "✨" },
      { key: "profile", label: "Admin Profile", href: "/admin/profile.html", icon: "👤" }
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
          <div class="brand-icon" style="background: linear-gradient(135deg, #1e3a8a, #0f172a); border: 1px solid rgba(255,255,255,0.2);">GU</div>
          <div class="brand-text">
            <h2>Galgotias LMS</h2>
            <span>Galgotias University Portal</span>
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

  // Populate Top Header with Global Backend Connection Indicator
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

        <div class="header-right" style="display: flex; align-items: center; gap: 0.75rem;">
          <!-- Notification Bell & Dropdown -->
          <div class="notification-bell-wrap" id="notification-bell-wrap">
            <button class="notification-bell-btn" id="notification-bell-btn" title="Notifications" aria-label="Notifications" onclick="window.common.toggleNotificationsMenu()">
              🔔
              <span class="notification-badge-count" id="notification-badge-count" style="display: none;">0</span>
            </button>
            <div class="notifications-menu" id="notifications-menu">
              <div class="notifications-menu-header">
                <h4>Notifications</h4>
                <button class="btn btn-secondary btn-sm" style="font-size: 0.72rem; padding: 0.2rem 0.5rem;" onclick="window.common.markAllNotificationsRead()">Mark all read</button>
              </div>
              <div class="notifications-menu-list" id="notifications-menu-list">
                <div style="padding: 1.5rem; text-align: center; color: #64748b; font-size: 0.8rem;">No new notifications</div>
              </div>
            </div>
          </div>

          <!-- Backend Connection Indicator (Green: connected, Red: unavailable, Yellow: checking) -->
          <div class="backend-status-indicator checking" id="backend-status-badge" title="Click to view/change backend URL" onclick="window.common.openApiConfigModal()">
            <span class="status-indicator-dot checking" id="backend-status-dot"></span>
            <span id="backend-status-text">Checking backend...</span>
          </div>
          <button class="btn btn-secondary btn-sm" id="top-logout-btn">
            Log Out
          </button>
        </div>
      </header>
    `;

    document.getElementById("top-logout-btn")?.addEventListener("click", () => {
      window.auth.logout();
    });

    startBackendHealthMonitor();
    startNotificationMonitor();

    // Close notifications menu when clicking outside
    document.addEventListener("click", (evt) => {
      const bellWrap = document.getElementById("notification-bell-wrap");
      const menu = document.getElementById("notifications-menu");
      if (menu && bellWrap && !bellWrap.contains(evt.target)) {
        menu.classList.remove("active");
      }
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

  // Inject GUL Buddy AI Floating Widget
  injectGulBuddyWidget();
}

/**
 * Injects GUL Buddy AI Floating Launcher & Interactive Drawer
 */
function injectGulBuddyWidget() {
  if (document.getElementById("gul-buddy-floating-btn")) return;

  const widgetHtml = `
    <!-- GUL Buddy Floating Launcher -->
    <div class="gul-buddy-launcher" id="gul-buddy-floating-btn" onclick="window.common.openGulBuddy()">
      <span class="gul-buddy-sparkle">✨</span>
      <span>GUL Buddy AI</span>
    </div>

    <!-- GUL Buddy Interactive Drawer -->
    <div class="gul-buddy-window" id="gul-buddy-window">
      <div class="gul-buddy-header">
        <div class="gul-buddy-title">
          <div class="gul-buddy-avatar">🎓</div>
          <div class="gul-buddy-header-text">
            <h3>GUL Buddy AI</h3>
            <p>Galgotias Academic Assistant</p>
          </div>
        </div>
        <button class="gul-buddy-close" onclick="window.common.closeGulBuddy()">&times;</button>
      </div>

      <div class="gul-buddy-messages" id="gul-buddy-message-stream">
        <div class="chat-bubble chat-bubble-bot">
          <h3>Welcome to GUL Buddy! 👋</h3>
          <p>I am your university AI companion powered by Galgotias LMS. Ask me anything about your course topics, code questions, exam preparation, or assignment guidelines!</p>
        </div>
      </div>

      <div class="chat-chips-bar">
        <button class="chat-chip" onclick="window.common.sendQuickPrompt('Explain Spring Security filters in Java')">☕ Spring Security</button>
        <button class="chat-chip" onclick="window.common.sendQuickPrompt('Generate 3 practice questions for my exams')">📝 Practice Quiz</button>
        <button class="chat-chip" onclick="window.common.sendQuickPrompt('What are the university attendance requirements?')">📊 Attendance Norms</button>
        <button class="chat-chip" onclick="window.common.sendQuickPrompt('How should I structure my assignment submission?')">📋 Assignment Tips</button>
      </div>

      <div class="gul-buddy-input-wrap">
        <input type="text" id="gul-buddy-user-input" class="gul-buddy-input" placeholder="Ask GUL Buddy a question..." onkeydown="if(event.key==='Enter') window.common.handleSendGulBuddy()" />
        <button class="gul-buddy-send-btn" id="gul-buddy-send-btn" onclick="window.common.handleSendGulBuddy()">Send</button>
      </div>
    </div>
  `;

  document.body.insertAdjacentHTML("beforeend", widgetHtml);
}

function openGulBuddy() {
  const win = document.getElementById("gul-buddy-window");
  if (win) {
    win.classList.add("active");
    const input = document.getElementById("gul-buddy-user-input");
    if (input) input.focus();
  }
}

function closeGulBuddy() {
  const win = document.getElementById("gul-buddy-window");
  if (win) {
    win.classList.remove("active");
  }
}

async function handleSendGulBuddy() {
  const input = document.getElementById("gul-buddy-user-input");
  if (!input) return;
  const prompt = input.value.trim();
  if (!prompt) return;

  input.value = "";
  await sendGulBuddyMessage(prompt);
}

function sendQuickPrompt(promptText) {
  openGulBuddy();
  sendGulBuddyMessage(promptText);
}

async function sendGulBuddyMessage(promptText) {
  const stream = document.getElementById("gul-buddy-message-stream");
  if (!stream) return;

  // Append user bubble
  const userBubble = document.createElement("div");
  userBubble.className = "chat-bubble chat-bubble-user";
  userBubble.innerText = promptText;
  stream.appendChild(userBubble);

  // Append temporary loading bot bubble
  const botBubble = document.createElement("div");
  botBubble.className = "chat-bubble chat-bubble-bot";
  botBubble.innerHTML = `<em>Thinking & generating academic insight...</em>`;
  stream.appendChild(botBubble);
  stream.scrollTop = stream.scrollHeight;

  try {
    const res = await window.api.post("/api/ai/chat", {
      prompt: promptText,
      context: "STUDENT_ASSISTANT"
    });

    const reply = res.reply || "I am ready to assist with your academic coursework.";
    // Simple markdown renderer for clean line breaks, bold and code
    botBubble.innerHTML = renderSimpleMarkdown(reply);
  } catch (err) {
    botBubble.innerHTML = `<span style="color: var(--danger);">⚠️ ${escapeHTML(err.message || "Failed to reach AI service. Please try again.")}</span>`;
  }
  stream.scrollTop = stream.scrollHeight;
}

function renderSimpleMarkdown(text) {
  if (!text) return "";
  let html = escapeHTML(text);

  // Headers ###
  html = html.replace(/^### (.*$)/gim, '<h3 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.35rem; color: #0f172a;">$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h3 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.4rem; color: #0f172a;">$1</h3>');

  // Bold **text**
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

  // Italics *text*
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');

  // Bullet points
  html = html.replace(/^\- (.*$)/gim, '<div style="margin-left: 0.75rem; margin-bottom: 0.25rem;">• $1</div>');

  // Line breaks
  html = html.replace(/\n\n/g, '<p style="margin: 0.4rem 0;"></p>');
  html = html.replace(/\n/g, '<br/>');

  return html;
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

/**
 * Global Automated Backend Health Monitor
 * Updates the header status dot:
 * Green: Backend connected
 * Yellow: Checking connection
 * Red: Backend unavailable
 * Checks on load and periodically without overloading the backend
 */
function startBackendHealthMonitor() {
  const checkHealth = async () => {
    const dot = document.getElementById("backend-status-dot");
    const text = document.getElementById("backend-status-text");
    const badge = document.getElementById("backend-status-badge");
    if (!dot || !text || !badge) return;

    try {
      const baseUrl = window.APP_CONFIG.getApiBaseUrl();
      const res = await fetch(`${baseUrl}/api/health`, {
        method: "GET",
        headers: { "Accept": "application/json" }
      });

      if (res.ok) {
        dot.className = "status-indicator-dot connected";
        badge.className = "backend-status-indicator connected";
        text.innerText = "Backend connected";
        badge.title = `Connected to Galgotias LMS Backend (${baseUrl})`;
      } else {
        dot.className = "status-indicator-dot disconnected";
        badge.className = "backend-status-indicator disconnected";
        text.innerText = "Backend unavailable";
        badge.title = `Backend returned status ${res.status}`;
      }
    } catch (err) {
      dot.className = "status-indicator-dot disconnected";
      badge.className = "backend-status-indicator disconnected";
      text.innerText = "Backend unavailable";
      badge.title = `Connection error: ${err.message}. Click to configure URL.`;
    }
  };

  // Run initial check
  checkHealth();

  // Periodic poll every 35s, only when tab is visible
  if (window._backendHealthInterval) {
    clearInterval(window._backendHealthInterval);
  }
  window._backendHealthInterval = setInterval(() => {
    if (document.visibilityState === "visible") {
      checkHealth();
    }
  }, 35000);
}

/**
 * Notifications Management Subsystem
 */
async function fetchUnreadNotificationsCount() {
  const token = window.auth?.getToken();
  if (!token) return;

  try {
    const res = await window.api.get("/api/notifications/unread-count");
    const count = (res && typeof res.count === "number") ? res.count : 0;
    const badge = document.getElementById("notification-badge-count");
    if (badge) {
      if (count > 0) {
        badge.innerText = count > 99 ? "99+" : count;
        badge.style.display = "inline-flex";
      } else {
        badge.style.display = "none";
      }
    }
  } catch (e) {
    // Silently ignore network hiccup during background poll
  }
}

async function loadNotificationsList() {
  const listContainer = document.getElementById("notifications-menu-list");
  if (!listContainer) return;

  try {
    const items = await window.api.get("/api/notifications");
    if (!items || items.length === 0) {
      listContainer.innerHTML = '<div style="padding: 1.5rem; text-align: center; color: #64748b; font-size: 0.8rem;">No notifications yet</div>';
      return;
    }

    listContainer.innerHTML = items.slice(0, 15).map(n => `
      <div class="notification-menu-item ${n.isRead ? "read" : "unread"}" onclick="window.common.handleNotificationClick(${n.id}, '${escapeHTML(n.link || "")}')">
        <div class="notification-menu-item-title">${escapeHTML(n.title)}</div>
        <div class="notification-menu-item-msg">${escapeHTML(n.message)}</div>
        <div class="notification-menu-item-time">${formatDate(n.createdAt)}</div>
      </div>
    `).join("");
  } catch (err) {
    listContainer.innerHTML = `<div style="padding: 1rem; text-align: center; color: #ef4444; font-size: 0.8rem;">Failed to load notifications</div>`;
  }
}

async function toggleNotificationsMenu() {
  const menu = document.getElementById("notifications-menu");
  if (!menu) return;

  const isActive = menu.classList.contains("active");
  if (isActive) {
    menu.classList.remove("active");
  } else {
    menu.classList.add("active");
    await loadNotificationsList();
  }
}

async function handleNotificationClick(id, link) {
  try {
    await window.api.put(`/api/notifications/${id}/read`, {});
    await fetchUnreadNotificationsCount();
  } catch (e) {
    // Continue navigation regardless
  }

  const menu = document.getElementById("notifications-menu");
  if (menu) menu.classList.remove("active");

  if (link && link.trim() !== "") {
    window.location.href = link;
  } else {
    await loadNotificationsList();
  }
}

async function markAllNotificationsRead() {
  try {
    await window.api.post("/api/notifications/read-all", {});
    showToast("All notifications marked as read", "success");
    await fetchUnreadNotificationsCount();
    await loadNotificationsList();
  } catch (e) {
    showToast("Failed to mark notifications as read", "error");
  }
}

function startNotificationMonitor() {
  fetchUnreadNotificationsCount();

  if (window._notificationPollInterval) {
    clearInterval(window._notificationPollInterval);
  }
  window._notificationPollInterval = setInterval(() => {
    if (document.visibilityState === "visible") {
      fetchUnreadNotificationsCount();
    }
  }, 30000);
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
  openApiConfigModal,
  startBackendHealthMonitor,
  startNotificationMonitor,
  fetchUnreadNotificationsCount,
  loadNotificationsList,
  toggleNotificationsMenu,
  handleNotificationClick,
  markAllNotificationsRead,
  openGulBuddy,
  closeGulBuddy,
  sendQuickPrompt,
  handleSendGulBuddy,
  sendGulBuddyMessage
};
