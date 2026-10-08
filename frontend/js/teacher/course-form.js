/**
 * Smart Learning Management System
 * Teacher Course Form (Create / Edit) Controller
 * Connects to POST /api/courses and PUT /api/courses/{id}
 */

let courseId = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "courses");

  const params = new URLSearchParams(window.location.search);
  courseId = params.get("id");

  if (courseId) {
    document.getElementById("form-heading").innerText = "Edit Course";
    document.getElementById("btn-label").innerText = "Update Course";
    await loadCourseForEdit();
  }

  setupForm();
});

async function loadCourseForEdit() {
  const errorAlert = document.getElementById("form-error-alert");
  try {
    const course = await window.api.get(`/api/courses/${courseId}`);
    document.getElementById("course-title").value = course.title || "";
    document.getElementById("course-description").value = course.description || "";
    document.getElementById("course-active").checked = course.active !== false;
  } catch (error) {
    errorAlert.innerText = error.message || "Failed to load course details";
    errorAlert.classList.remove("hidden");
  }
}

function setupForm() {
  const form = document.getElementById("course-form");
  const titleInput = document.getElementById("course-title");
  const descInput = document.getElementById("course-description");
  const activeInput = document.getElementById("course-active");
  const submitBtn = document.getElementById("save-course-btn");
  const btnSpinner = document.getElementById("btn-spinner");
  const btnLabel = document.getElementById("btn-label");
  const errorAlert = document.getElementById("form-error-alert");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    errorAlert.classList.add("hidden");
    document.getElementById("title-error").classList.add("hidden");
    document.getElementById("desc-error").classList.add("hidden");

    const title = titleInput.value.trim();
    const description = descInput.value.trim();
    const active = activeInput.checked;

    let isValid = true;
    if (!title) {
      document.getElementById("title-error").innerText = "Course title is required.";
      document.getElementById("title-error").classList.remove("hidden");
      isValid = false;
    }

    if (!description) {
      document.getElementById("desc-error").innerText = "Course description is required.";
      document.getElementById("desc-error").classList.remove("hidden");
      isValid = false;
    }

    if (!isValid) return;

    submitBtn.disabled = true;
    btnSpinner.classList.remove("hidden");
    btnLabel.innerText = courseId ? "Updating..." : "Creating...";

    const payload = {
      title,
      description,
      active
    };

    try {
      if (courseId) {
        await window.api.put(`/api/courses/${courseId}`, payload);
        window.common.showToast("Course updated successfully!", "success");
      } else {
        await window.api.post("/api/courses", payload);
        window.common.showToast("Course created successfully!", "success");
      }

      setTimeout(() => {
        window.location.href = "/teacher/courses.html";
      }, 700);
    } catch (error) {
      errorAlert.innerText = error.message || "Failed to save course. Please check all fields.";
      errorAlert.classList.remove("hidden");
      submitBtn.disabled = false;
      btnSpinner.classList.add("hidden");
      btnLabel.innerText = courseId ? "Update Course" : "Save Course";
    }
  });
}
