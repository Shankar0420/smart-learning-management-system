/**
 * Smart Learning Management System
 * Admin Profile Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("ADMIN")) return;
  window.common.initDashboardLayout("ADMIN", "profile");

  let user = window.auth.getCurrentUser();

  try {
    const remoteUser = await window.api.get("/api/users/me").catch(() => null);
    if (remoteUser) {
      user = { ...user, ...remoteUser };
      localStorage.setItem("lms_user", JSON.stringify(user));
    }
  } catch (e) {
    // Continue with existing user
  }

  if (user) {
    const name = user.name || "System Administrator";
    const email = user.email || "admin@galgotiasuniversity.edu.in";

    document.getElementById("admin-profile-name").innerText = name;
    document.getElementById("admin-profile-email").innerText = email;
    document.getElementById("admin-profile-avatar").innerText = name.charAt(0).toUpperCase();
  }
});
