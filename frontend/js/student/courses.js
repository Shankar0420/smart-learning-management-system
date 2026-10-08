/**
 * Smart Learning Management System
 * Student Courses Controller
 * Connects to GET /api/student/courses, GET /api/courses, and POST /api/courses/{id}/enroll
 */

let currentTab = "enrolled"; // "enrolled" or "all"
let enrolledCourses = [];
let allCourses = [];
let enrolledCourseIds = new Set();

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("STUDENT")) return;
  window.common.initDashboardLayout("STUDENT", "courses");

  setupTabs();
  await loadCoursesData();
});

function setupTabs() {
  const tabEnrolled = document.getElementById("tab-enrolled");
  const tabAll = document.getElementById("tab-all");

  tabEnrolled.addEventListener("click", () => {
    currentTab = "enrolled";
    tabEnrolled.className = "btn btn-sm btn-primary";
    tabAll.className = "btn btn-sm btn-secondary";
    renderCourses();
  });

  tabAll.addEventListener("click", () => {
    currentTab = "all";
    tabAll.className = "btn btn-sm btn-primary";
    tabEnrolled.className = "btn btn-sm btn-secondary";
    renderCourses();
  });
}

async function loadCoursesData() {
  const container = document.getElementById("courses-container");
  const errorArea = document.getElementById("courses-error-area");
  errorArea.classList.add("hidden");

  try {
    // Parallel fetch for enrolled courses and all courses
    const [enrolledRes, allRes] = await Promise.all([
      window.api.get("/api/student/courses").catch(err => {
        console.warn("Could not fetch enrolled courses:", err);
        return [];
      }),
      window.api.get("/api/courses").catch(err => {
        console.warn("Could not fetch all courses:", err);
        return [];
      })
    ]);

    enrolledCourses = Array.isArray(enrolledRes) ? enrolledRes : (enrolledRes.courses || []);
    allCourses = Array.isArray(allRes) ? allRes : (allRes.courses || []);

    // Build set of enrolled IDs
    enrolledCourseIds.clear();
    enrolledCourses.forEach(c => {
      const id = c.courseId || c.id || c.course?.id;
      if (id) enrolledCourseIds.add(String(id));
    });

    renderCourses();
  } catch (error) {
    errorArea.innerText = error.message;
    errorArea.classList.remove("hidden");
    window.common.renderErrorState(container, error.message, () => loadCoursesData());
  }
}

function renderCourses() {
  const container = document.getElementById("courses-container");
  const listToRender = currentTab === "enrolled" ? enrolledCourses : allCourses;

  if (listToRender.length === 0) {
    if (currentTab === "enrolled") {
      window.common.renderEmptyState(
        container,
        "No Enrolled Courses Yet",
        "You haven't enrolled in any courses. Browse available courses to start learning.",
        '<button class="btn btn-primary btn-sm" onclick="document.getElementById(\'tab-all\').click()">Browse All Courses</button>'
      );
    } else {
      window.common.renderEmptyState(
        container,
        "No Courses Available",
        "There are currently no active courses published by faculty."
      );
    }
    return;
  }

  container.innerHTML = listToRender.map(item => {
    const course = item.course || item;
    const id = course.id || item.courseId;
    const isEnrolled = enrolledCourseIds.has(String(id));
    const teacherName = course.teacherName || course.teacher?.name || course.instructor || "Faculty";
    const title = course.title || "Untitled Course";
    const description = course.description || "Comprehensive university course syllabus and learning materials.";
    const progress = item.progress ?? item.percentage ?? 0;

    return `
      <div class="course-card">
        <div class="course-card-banner">
          <span class="course-category-tag">Computer Science</span>
        </div>
        <div class="course-card-content">
          <h3 class="course-card-title">${window.common.escapeHTML(title)}</h3>
          <p class="course-card-desc">${window.common.escapeHTML(description)}</p>

          ${currentTab === "enrolled" ? `
            <div class="progress-container" style="margin-bottom: 0.75rem;">
              <div class="progress-header">
                <span>Progress</span>
                <span>${progress}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-bar-fill ${progress === 100 ? 'success' : ''}" style="width: ${progress}%;"></div>
              </div>
            </div>
          ` : ""}

          <div class="course-card-meta">
            <span>👨‍🏫 ${window.common.escapeHTML(teacherName)}</span>
            ${isEnrolled ? '<span class="badge badge-success">Enrolled</span>' : '<span class="badge badge-neutral">Available</span>'}
          </div>

          <div class="course-card-actions">
            <a href="/student/course-details.html?id=${id}" class="btn btn-secondary btn-sm flex-1 justify-center">
              View Details
            </a>
            ${isEnrolled ? `
              <a href="/student/learning.html?courseId=${id}" class="btn btn-primary btn-sm flex-1 justify-center">
                Continue Learning
              </a>
            ` : `
              <button class="btn btn-primary btn-sm flex-1 justify-center" onclick="handleEnroll('${id}', this)">
                Enroll Now
              </button>
            `}
          </div>
        </div>
      </div>
    `;
  }).join("");
}

// Global Enroll Handler
window.handleEnroll = async function(courseId, buttonEl) {
  if (!courseId) return;

  const originalText = buttonEl.innerText;
  buttonEl.disabled = true;
  buttonEl.innerHTML = '<span class="spinner"></span> Enrolling...';

  try {
    await window.api.post(`/api/courses/${courseId}/enroll`, {});
    window.common.showToast("Successfully enrolled in course!", "success");
    enrolledCourseIds.add(String(courseId));
    // Reload data
    await loadCoursesData();
  } catch (error) {
    window.common.showToast(error.message || "Failed to enroll in course", "error");
    buttonEl.disabled = false;
    buttonEl.innerText = originalText;
  }
};
