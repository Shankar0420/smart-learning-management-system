/**
 * Smart Learning Management System
 * Faculty Profile & Documents Controller
 */

let facultyUser = null;
let facultyDocs = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!window.auth.requireRole("TEACHER")) return;
  window.common.initDashboardLayout("TEACHER", "profile");

  setupProfileForms();
  await loadFacultyProfile();
  await loadFacultyDocuments();
});

async function loadFacultyProfile() {
  let user = window.auth.getCurrentUser();

  try {
    const remote = await window.api.get(`/api/faculty/${user.id}`).catch(async () => {
      return await window.api.get("/api/users/me");
    });

    if (remote) {
      facultyUser = remote;
      user = { ...user, ...remote };
      localStorage.setItem("lms_user", JSON.stringify(user));
    }
  } catch (e) {
    facultyUser = user;
  }

  if (user) {
    const name = user.name || "Faculty Member";
    const email = user.email || "faculty@galgotiasuniversity.edu.in";
    const status = user.status || "ACTIVE";
    const profId = user.professionalId || "Assigned on Approval";
    const empId = user.employeeId || "N/A";

    document.getElementById("teacher-profile-name").innerText = name;
    document.getElementById("teacher-profile-email").innerText = email;
    document.getElementById("teacher-profile-status").innerText = status;
    document.getElementById("teacher-profile-avatar").innerText = name.charAt(0).toUpperCase();

    const idEl = document.getElementById("teacher-profile-id");
    if (idEl) idEl.innerText = profId;

    const empEl = document.getElementById("teacher-profile-emp-id");
    if (empEl) empEl.innerText = empId;

    const desigEl = document.getElementById("teacher-profile-designation");
    if (desigEl) desigEl.innerText = (user.designation || "ASSISTANT PROFESSOR").toUpperCase();

    const phoneEl = document.getElementById("teacher-profile-phone");
    if (phoneEl) phoneEl.innerText = user.phone || "Not Provided";

    const qualEl = document.getElementById("teacher-profile-qualification");
    if (qualEl) qualEl.innerText = user.qualification || "M.Tech / Ph.D.";

    const specEl = document.getElementById("teacher-profile-specialization");
    if (specEl) specEl.innerText = user.specialization || "Computer Science & Engineering";

    const deptEl = document.getElementById("teacher-profile-department");
    if (deptEl) deptEl.innerText = user.department || "School of Computing Science";

    const joinEl = document.getElementById("teacher-profile-joining");
    if (joinEl) joinEl.innerText = user.joiningDate ? window.common.formatDate(user.joiningDate).split(",")[0] : "Verified Faculty";
  }
}

async function loadFacultyDocuments() {
  const container = document.getElementById("faculty-docs-container");
  if (!container) return;

  const user = window.auth.getCurrentUser();
  if (!user || !user.id) return;

  try {
    const docs = await window.api.get(`/api/faculty/${user.id}/documents`);
    facultyDocs = Array.isArray(docs) ? docs : [];

    if (facultyDocs.length === 0) {
      window.common.renderEmptyState(
        container,
        "No Documents Uploaded",
        "Upload your curriculum vitae, educational degrees, and appointment records for official administration verification.",
        '<button class="btn btn-primary btn-sm" onclick="openUploadDocModal()">+ Upload First Document</button>'
      );
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Document Type</th>
              <th>Document Title</th>
              <th>File Name</th>
              <th>Uploaded Date</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            ${facultyDocs.map(d => {
              const typeLabel = formatDocType(d.documentType);
              const ext = (d.fileName || d.fileUrl || "pdf").split(".").pop().toUpperCase();

              return `
                <tr>
                  <td>
                    <span class="badge badge-info" style="font-size: 0.75rem;">${window.common.escapeHTML(typeLabel)}</span>
                  </td>
                  <td>
                    <strong>${window.common.escapeHTML(d.title)}</strong>
                    ${d.description ? `<p class="text-muted" style="font-size: 0.78rem; margin: 0.2rem 0 0 0;">${window.common.escapeHTML(d.description)}</p>` : ''}
                  </td>
                  <td>
                    <code style="font-size: 0.75rem; background: rgba(30,58,138,0.08); color: var(--primary); padding: 0.12rem 0.35rem; border-radius: 4px;">
                      ${window.common.escapeHTML(d.fileName || "document")}
                    </code>
                    <span class="badge badge-neutral" style="font-size: 0.7rem; margin-left: 0.25rem;">${ext}</span>
                  </td>
                  <td class="text-muted tabular-nums" style="font-size: 0.8rem;">${window.common.formatDate(d.uploadedAt || d.createdAt)}</td>
                  <td style="text-align: right;">
                    <div class="flex items-center justify-end gap-2">
                      <button class="btn btn-secondary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;" onclick="window.previewFacultyDoc(${d.id})">
                        👁️ Preview
                      </button>
                      <button class="btn btn-secondary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;" onclick="window.downloadFacultyDoc(${d.id})">
                        ⬇️ Download
                      </button>
                      <button class="btn btn-danger-outline btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;" onclick="window.deleteFacultyDoc(${d.id}, '${window.common.escapeHTML(d.title)}')">
                        ✕ Delete
                      </button>
                    </div>
                  </td>
                </tr>
              `;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    window.common.renderErrorState(container, err.message, () => loadFacultyDocuments());
  }
}

function formatDocType(type) {
  switch (type) {
    case "RESUME": return "CV / Resume";
    case "ACADEMIC_RECORD": return "Degree / Transcript";
    case "CERTIFICATE": return "Certification";
    case "EXPERIENCE_LETTER": return "Experience Letter";
    case "RESEARCH_PAPER": return "Publication";
    default: return type ? type.replace(/_/g, " ") : "Document";
  }
}

function setupProfileForms() {
  // Edit Profile Form
  const profileForm = document.getElementById("edit-profile-form");
  if (profileForm) {
    profileForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const user = window.auth.getCurrentUser();
      const name = document.getElementById("edit-name").value.trim();
      const phone = document.getElementById("edit-phone").value.trim();
      const designation = document.getElementById("edit-designation").value.trim();
      const qualification = document.getElementById("edit-qualification").value.trim();
      const specialization = document.getElementById("edit-specialization").value.trim();
      const joiningDate = document.getElementById("edit-joining").value;

      const btn = document.getElementById("save-profile-btn");
      btn.disabled = true;

      try {
        await window.api.put(`/api/faculty/${user.id}/profile`, {
          name,
          phone,
          designation,
          qualification,
          specialization,
          joiningDate: joiningDate || null
        });

        window.common.showToast("Faculty profile updated successfully!", "success");
        window.common.closeModal("edit-profile-modal");
        await loadFacultyProfile();
      } catch (err) {
        window.common.showToast(err.message || "Failed to update profile", "error");
      } finally {
        btn.disabled = false;
      }
    });
  }

  // Upload Document Form
  const docForm = document.getElementById("upload-doc-form");
  if (docForm) {
    docForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const user = window.auth.getCurrentUser();
      const docType = document.getElementById("doc-type-select").value;
      const title = document.getElementById("doc-title-input").value.trim();
      const desc = document.getElementById("doc-desc-input").value.trim();
      const fileInput = document.getElementById("doc-file-input");

      if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        window.common.showToast("Please choose a file to upload.", "warning");
        return;
      }

      const btn = document.getElementById("save-doc-btn");
      btn.disabled = true;
      btn.innerText = "Uploading...";

      try {
        const formData = new FormData();
        formData.append("documentType", docType);
        formData.append("title", title);
        if (desc) formData.append("description", desc);
        formData.append("file", fileInput.files[0]);

        await window.api.post(`/api/faculty/${user.id}/documents`, formData);
        window.common.showToast("Document uploaded successfully!", "success");
        window.common.closeModal("upload-doc-modal");
        docForm.reset();
        await loadFacultyDocuments();
      } catch (err) {
        window.common.showToast(err.message || "Failed to upload document", "error");
      } finally {
        btn.disabled = false;
        btn.innerText = "Upload Document";
      }
    });
  }
}

window.openEditProfileModal = function() {
  const user = facultyUser || window.auth.getCurrentUser();
  if (user) {
    document.getElementById("edit-name").value = user.name || "";
    document.getElementById("edit-phone").value = user.phone || "";
    document.getElementById("edit-designation").value = user.designation || "";
    document.getElementById("edit-qualification").value = user.qualification || "";
    document.getElementById("edit-specialization").value = user.specialization || "";
    document.getElementById("edit-joining").value = user.joiningDate ? user.joiningDate.split("T")[0] : "";
  }
  window.common.openModal("edit-profile-modal");
};

window.openUploadDocModal = function() {
  const form = document.getElementById("upload-doc-form");
  if (form) form.reset();
  window.common.openModal("upload-doc-modal");
};

window.downloadFacultyDoc = function(docId) {
  const user = window.auth.getCurrentUser();
  const baseUrl = window.APP_CONFIG.getApiBaseUrl();
  window.open(`${baseUrl}/api/faculty/${user.id}/documents/${docId}/download`, "_blank");
};

window.previewFacultyDoc = function(docId) {
  const user = window.auth.getCurrentUser();
  const doc = facultyDocs.find(d => d.id === docId);
  const title = doc ? doc.title : "Document Preview";
  const ext = (doc?.fileName || doc?.fileUrl || "pdf").split(".").pop().toLowerCase();
  const baseUrl = window.APP_CONFIG.getApiBaseUrl();

  const titleEl = document.getElementById("doc-preview-title");
  const bodyEl = document.getElementById("doc-preview-body");
  if (titleEl) titleEl.innerText = title;

  const previewUrl = `${baseUrl}/api/faculty/${user.id}/documents/${docId}/preview`;

  if (ext === "pdf") {
    bodyEl.innerHTML = `<iframe src="${previewUrl}" style="width: 100%; height: 100%; border: none;"></iframe>`;
  } else if (["jpg", "jpeg", "png", "webp"].includes(ext)) {
    bodyEl.innerHTML = `<div style="display:flex; justify-content:center; align-items:center; height:100%;"><img src="${previewUrl}" style="max-width:100%; max-height:100%; object-fit:contain;"/></div>`;
  } else {
    bodyEl.innerHTML = `
      <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: white; gap: 1rem; padding: 2rem; text-align: center;">
        <div style="font-size: 3rem;">📄</div>
        <h3>${window.common.escapeHTML(title)}</h3>
        <p style="color: #cbd5e1; max-width: 480px; font-size: 0.9rem;">
          Document format: ${ext.toUpperCase()}. Download the file to view its full contents.
        </p>
        <button class="btn btn-primary" onclick="window.downloadFacultyDoc(${docId})">⬇️ Download File</button>
      </div>
    `;
  }

  window.common.openModal("doc-preview-modal");
};

window.deleteFacultyDoc = function(docId, title) {
  const user = window.auth.getCurrentUser();
  window.common.confirmAction(`Are you sure you want to delete "${title}"?`, async () => {
    try {
      await window.api.delete(`/api/faculty/${user.id}/documents/${docId}`);
      window.common.showToast("Faculty document deleted successfully!", "success");
      await loadFacultyDocuments();
    } catch (err) {
      window.common.showToast(err.message || "Failed to delete document", "error");
    }
  });
};
