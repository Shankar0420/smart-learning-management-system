/**
 * Smart Learning Management System
 * Centralized Authentication & Authorization State
 */

const AUTH_TOKEN_KEY = "lms_token";
const AUTH_USER_KEY = "lms_user";

/**
 * Returns JWT or Bearer token string
 */
function getToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

/**
 * Returns current authenticated user object or null
 */
function getCurrentUser() {
  const userJson = localStorage.getItem(AUTH_USER_KEY);
  if (!userJson) return null;
  try {
    return JSON.parse(userJson);
  } catch (e) {
    return null;
  }
}

/**
 * Check if user is currently logged in
 */
function isAuthenticated() {
  const token = getToken();
  const user = getCurrentUser();
  return Boolean(token && user);
}

/**
 * Check if the authenticated user has a specific role
 */
function hasRole(role) {
  const user = getCurrentUser();
  if (!user || !user.role) return false;
  return user.role.toUpperCase() === role.toUpperCase();
}

/**
 * Map role to correct dashboard URL
 */
function getRoleDashboard(role) {
  const normalized = (role || "").toUpperCase();
  switch (normalized) {
    case "ADMIN":
      return "/admin/dashboard.html";
    case "TEACHER":
      return "/teacher/dashboard.html";
    case "STUDENT":
    default:
      return "/student/dashboard.html";
  }
}

/**
 * Saves authentication response from Spring Boot backend.
 * Adapts to various standard backend response structures:
 * { token, user: { role, name, email, id } } OR
 * { token, role, email, name, id } OR
 * { accessToken, role, email, ... }
 */
function saveAuth(authResponse) {
  if (!authResponse) return false;

  let token = authResponse.token || authResponse.accessToken || authResponse.jwt;
  let user = authResponse.user;

  if (!user && (authResponse.role || authResponse.email)) {
    user = {
      id: authResponse.id || authResponse.userId,
      name: authResponse.name || authResponse.fullName || authResponse.username || authResponse.email.split("@")[0],
      email: authResponse.email,
      role: (authResponse.role || "STUDENT").toUpperCase(),
      status: authResponse.status || "ACTIVE",
      createdAt: authResponse.createdAt || authResponse.createdDate
    };
  } else if (user) {
    user.role = (user.role || "STUDENT").toUpperCase();
  }

  if (token) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  }
  if (user) {
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  }

  return true;
}

/**
 * Logs out user, calls backend POST /api/auth/logout if configured,
 * clears storage, and redirects to login page.
 */
async function logout() {
  try {
    if (window.api && getToken()) {
      await window.api.post("/api/auth/logout", {}).catch(() => {});
    }
  } catch (err) {
    // Ignore network error during logout
  } finally {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
    window.location.href = "/pages/login.html";
  }
}

/**
 * Enforces authentication on protected pages.
 * Redirects to login if user is not authenticated.
 */
function requireAuth() {
  if (!isAuthenticated()) {
    const returnUrl = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = `/pages/login.html?returnUrl=${returnUrl}`;
    return false;
  }
  return true;
}

/**
 * Enforces role-based navigation.
 * If user does not match the required role, redirects to their authorized dashboard.
 */
function requireRole(expectedRole) {
  if (!requireAuth()) return false;

  const user = getCurrentUser();
  if (!hasRole(expectedRole)) {
    // Redirect to their own dashboard
    const correctDashboard = getRoleDashboard(user ? user.role : null);
    window.location.href = correctDashboard;
    return false;
  }
  return true;
}

// Export to window
window.auth = {
  getToken,
  getCurrentUser,
  isAuthenticated,
  hasRole,
  getRoleDashboard,
  saveAuth,
  logout,
  requireAuth,
  requireRole
};
