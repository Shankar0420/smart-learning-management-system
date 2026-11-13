/**
 * Galgotias LMS - Student Study Materials Browser
 */

let currentCategory = "ALL";
let searchQuery = "";
let selectedSemester = "";
let allMaterials = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireAuth(["STUDENT", "TEACHER", "ADMIN"])) return;

  window.common.initDashboardLayout("STUDENT", "study-materials");

  initEventListeners();
  await loadMaterials();
});

function initEventListeners() {
  // Category tabs
  const tabButtons = document.querySelectorAll(".category-tab-btn");
  tabButtons.forEach(btn => {
    btn.addEventListener("click", async () => {
      tabButtons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      currentCategory = btn.getAttribute("data-category") || "ALL";
      await loadMaterials();
    });
  });

  // Search input & button
  document.getElementById("material-search-btn")?.addEventListener("click", async () => {
    searchQuery = document.getElementById("material-search-input")?.value.trim() || "";
    selectedSemester = document.getElementById("material-semester-select")?.value || "";
    await loadMaterials();
  });

  document.getElementById("material-search-input")?.addEventListener("keydown", async (e) => {
    if (e.key === "Enter") {
      searchQuery = e.target.value.trim();
      selectedSemester = document.getElementById("material-semester-select")?.value || "";
      await loadMaterials();
    }
  });

  // Reset button
  document.getElementById("material-reset-btn")?.addEventListener("click", async () => {
    searchQuery = "";
    selectedSemester = "";
    const searchInput = document.getElementById("material-search-input");
    const semSelect = document.getElementById("material-semester-select");
    if (searchInput) searchInput.value = "";
    if (semSelect) semSelect.value = "";
    await loadMaterials();
  });
}

async function loadMaterials() {
  const container = document.getElementById("materials-container");
  const errorArea = document.getElementById("materials-error-area");
  if (errorArea) errorArea.classList.add("hidden");

  window.common.renderLoading(container, "Fetching academic materials from MariaDB...");

  try {
    let materials = [];
    if (currentCategory === "BOOKMARKS") {
      materials = await window.api.get("/api/materials/bookmarks");
    } else {
      let endpoint = `/api/materials/search?query=${encodeURIComponent(searchQuery)}`;
      if (currentCategory !== "ALL") {
        endpoint += `&category=${encodeURIComponent(currentCategory)}`;
      }
      if (selectedSemester) {
        endpoint += `&semester=${encodeURIComponent(selectedSemester)}`;
      }
      materials = await window.api.get(endpoint);
    }

    allMaterials = Array.isArray(materials) ? materials : [];
    renderMaterials(allMaterials);
  } catch (err) {
    if (errorArea) {
      errorArea.innerText = `Error loading study materials: ${err.message}`;
      errorArea.classList.remove("hidden");
    }
    window.common.renderErrorState(container, err.message, () => loadMaterials());
  }
}

function renderMaterials(items) {
  const container = document.getElementById("materials-container");
  if (!container) return;

  if (items.length === 0) {
    window.common.renderEmptyState(
      container,
      "No Study Materials Found",
      currentCategory === "BOOKMARKS"
        ? "You haven't bookmarked any study materials yet. Click the star icon on any material to save it here."
        : "No materials match your current category, search or semester filter."
    );
    return;
  }

  container.innerHTML = items.map(m => {
    const ext = getFileExtension(m.fileName || m.fileUrl || "");
    const fileTypeBadgeClass = getFileTypeBadgeClass(ext);
    const categoryLabel = formatCategoryLabel(m.category);
    const formattedDate = window.common.formatDate(m.createdAt);
    const isBookmarked = !!m.bookmarked;

    return `
      <div class="card" style="display: flex; flex-direction: column; justify-content: space-between; border-radius: 10px; overflow: hidden; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid var(--border-color);">
        <div style="padding: 1.25rem;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; margin-bottom: 0.75rem;">
            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <span class="file-type-badge ${fileTypeBadgeClass}">${window.common.escapeHTML(ext || "FILE")}</span>
              <span class="badge badge-info" style="font-size: 0.72rem;">${window.common.escapeHTML(categoryLabel)}</span>
              ${m.semester ? `<span class="badge" style="background:#e0e7ff; color:#3730a3; font-size: 0.72rem;">Sem ${m.semester}</span>` : ""}
            </div>
            <button class="btn btn-secondary btn-sm" onclick="window.toggleBookmark(${m.id}, this)" title="${isBookmarked ? "Remove Bookmark" : "Bookmark Material"}" style="padding: 0.25rem 0.5rem; font-size: 0.95rem; line-height: 1; border: none; background: ${isBookmarked ? "#fef3c7" : "transparent"}; color: ${isBookmarked ? "#d97706" : "#94a3b8"};">
              ${isBookmarked ? "★" : "☆"}
            </button>
          </div>

          <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--dark); margin-bottom: 0.4rem; line-height: 1.35;">
            ${window.common.escapeHTML(m.title)}
          </h3>

          <p class="text-muted" style="font-size: 0.82rem; margin-bottom: 0.75rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
            ${window.common.escapeHTML(m.description || "No specific description provided.")}
          </p>

          <div style="font-size: 0.75rem; color: #64748b; display: flex; flex-direction: column; gap: 0.25rem;">
            <div><strong>Subject:</strong> ${window.common.escapeHTML(m.subject || "General Academic")}</div>
            ${m.department ? `<div><strong>Dept:</strong> ${window.common.escapeHTML(m.department)}</div>` : ""}
          </div>
        </div>

        <div style="padding: 0.85rem 1.25rem; background: #f8fafc; border-top: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
          <div style="font-size: 0.72rem; color: #94a3b8;">
            <span>👁️ ${m.viewCount || 0}</span> &bull; 
            <span>⬇️ ${m.downloadCount || 0}</span> &bull; 
            <span>${formattedDate}</span>
          </div>
          <div style="display: flex; gap: 0.4rem;">
            <button class="btn btn-secondary btn-sm" style="font-size: 0.78rem; padding: 0.3rem 0.6rem;" onclick="window.previewMaterial(${m.id})">
              👁️ Preview
            </button>
            <button class="btn btn-primary btn-sm" style="font-size: 0.78rem; padding: 0.3rem 0.6rem;" onclick="window.downloadMaterial(${m.id})">
              ⬇️ Download
            </button>
          </div>
        </div>
      </div>
    `;
  }).join("");
}

function getFileExtension(filename) {
  if (!filename) return "file";
  const parts = filename.split(".");
  return parts.length > 1 ? parts.pop().toLowerCase() : "file";
}

function getFileTypeBadgeClass(ext) {
  if (ext === "pdf") return "file-type-pdf";
  if (ext === "doc" || ext === "docx") return "file-type-doc";
  if (ext === "ppt" || ext === "pptx") return "file-type-ppt";
  if (ext === "zip" || ext === "rar" || ext === "tar") return "file-type-zip";
  if (ext === "mp4" || ext === "mkv" || ext === "avi") return "file-type-mp4";
  return "file-type-other";
}

function formatCategoryLabel(cat) {
  if (!cat) return "Course Material";
  switch (cat) {
    case "LECTURE_NOTES": return "Lecture Notes";
    case "PRESENTATION": return "Class Presentation";
    case "REFERENCE_BOOK": return "Reference Book";
    case "PREVIOUS_YEAR_QUESTIONS": return "Previous Year Questions";
    case "LAB_MANUAL": return "Lab Manual";
    case "SYLLABUS": return "Syllabus";
    case "ASSIGNMENT_REFERENCE": return "Assignment Reference";
    default: return cat.replace(/_/g, " ");
  }
}

window.toggleBookmark = async function(materialId, btn) {
  try {
    const res = await window.api.post(`/api/materials/${materialId}/bookmark`, {});
    const isNowBookmarked = res && res.bookmarked;
    if (btn) {
      btn.innerHTML = isNowBookmarked ? "★" : "☆";
      btn.style.color = isNowBookmarked ? "#d97706" : "#94a3b8";
      btn.style.background = isNowBookmarked ? "#fef3c7" : "transparent";
      btn.title = isNowBookmarked ? "Remove Bookmark" : "Bookmark Material";
    }
    window.common.showToast(isNowBookmarked ? "Saved to Bookmarks" : "Bookmark removed", "success");
    if (currentCategory === "BOOKMARKS") {
      await loadMaterials();
    }
  } catch (err) {
    window.common.showToast(err.message || "Failed to update bookmark", "error");
  }
};

window.downloadMaterial = function(materialId) {
  const baseUrl = window.APP_CONFIG.getApiBaseUrl();
  const token = window.auth.getToken();
  // Using direct window navigation or fetch to trigger download counter and file stream
  window.open(`${baseUrl}/api/materials/${materialId}/download`, "_blank");
};

window.previewMaterial = async function(materialId) {
  const material = allMaterials.find(m => m.id === materialId);
  const title = material ? material.title : "Document Preview";
  const ext = material ? getFileExtension(material.fileName || material.fileUrl) : "pdf";
  const baseUrl = window.APP_CONFIG.getApiBaseUrl();

  const modalTitle = document.getElementById("preview-modal-title");
  const modalMeta = document.getElementById("preview-modal-meta");
  const modalBody = document.getElementById("preview-modal-body");
  const downloadBtn = document.getElementById("preview-modal-download-btn");

  if (modalTitle) modalTitle.innerText = title;
  if (modalMeta) {
    modalMeta.innerText = `${formatCategoryLabel(material?.category)} &bull; ${material?.fileName || "Document"}`;
  }
  if (downloadBtn) {
    downloadBtn.onclick = () => window.downloadMaterial(materialId);
  }

  const previewUrl = `${baseUrl}/api/materials/${materialId}/preview`;

  if (ext === "pdf") {
    modalBody.innerHTML = `
      <iframe src="${previewUrl}" style="width: 100%; height: 100%; border: none;" title="${window.common.escapeHTML(title)}"></iframe>
    `;
  } else if (["jpg", "jpeg", "png", "gif", "webp", "svg"].includes(ext)) {
    modalBody.innerHTML = `
      <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #0f172a; padding: 1rem;">
        <img src="${previewUrl}" alt="${window.common.escapeHTML(title)}" style="max-width: 100%; max-height: 100%; object-fit: contain; border-radius: 4px;" />
      </div>
    `;
  } else {
    modalBody.innerHTML = `
      <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: white; gap: 1rem; padding: 2rem; text-align: center;">
        <div style="font-size: 3rem;">📄</div>
        <h3 style="font-size: 1.25rem;">${window.common.escapeHTML(title)}</h3>
        <p style="color: #cbd5e1; max-width: 500px; font-size: 0.9rem;">
          Inline preview is optimized for PDF and image files. For ${ext.toUpperCase()} presentations and documents, please download the file to view it in your desktop application.
        </p>
        <button class="btn btn-primary" onclick="window.downloadMaterial(${materialId})">⬇️ Download ${ext.toUpperCase()} File</button>
      </div>
    `;
  }

  window.common.openModal("material-preview-modal");
};

