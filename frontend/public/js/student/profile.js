/**
 * Smart Learning Management System
 * Student Profile Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "profile");

  let user = window.auth.getCurrentUser();

  // Optionally refresh profile data if backend exposes user profile endpoint
  try {
    const remoteUser = await window.api.get("/api/users/me").catch(() => null);
    if (remoteUser) {
      user = { ...user, ...remoteUser };
      localStorage.setItem("lms_user", JSON.stringify(user));
    }
  } catch (e) {
    // Continue with existing user state
  }

  if (user) {
    const name = user.name || "Student User";
    const email = user.email || "student@galgotiasuniversity.edu.in";
    const role = (user.role || "STUDENT").toUpperCase();
    const status = user.status || "ACTIVE";
    const created = user.createdAt || user.createdDate;

    document.getElementById("profile-name").innerText = name;
    document.getElementById("profile-email").innerText = email;
    document.getElementById("profile-role").innerText = role;
    document.getElementById("profile-status-text").innerText = status;
    document.getElementById("profile-status-badge").innerText = status;
    document.getElementById("profile-avatar").innerText = name.charAt(0).toUpperCase();

    if (created) {
      document.getElementById("profile-created").innerText = window.common.formatDate(created);
    } else {
      document.getElementById("profile-created").innerText = "Academic Year 2026";
    }
  }
});
