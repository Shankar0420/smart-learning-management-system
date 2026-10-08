/**
 * Smart Learning Management System
 * Teacher Profile Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "profile");

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
    const name = user.name || "Faculty Member";
    const email = user.email || "faculty@galgotiasuniversity.edu.in";
    const status = user.status || "ACTIVE";

    document.getElementById("teacher-profile-name").innerText = name;
    document.getElementById("teacher-profile-email").innerText = email;
    document.getElementById("teacher-profile-status").innerText = status;
    document.getElementById("teacher-profile-avatar").innerText = name.charAt(0).toUpperCase();
  }
});
