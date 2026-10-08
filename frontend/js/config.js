/**
 * Smart Learning Management System
 * Centralized API & Application Configuration
 */

// Default Spring Boot backend URL as required by specification
const DEFAULT_API_BASE_URL = "http://localhost:8080";

/**
 * Retrieves the active backend API Base URL.
 * Checks localStorage for user-overridden URL or defaults to http://localhost:8080.
 */
function getApiBaseUrl() {
  const customUrl = localStorage.getItem("LMS_API_BASE_URL");
  if (customUrl && customUrl.trim() !== "") {
    return customUrl.trim().replace(/\/+$/, "");
  }
  return DEFAULT_API_BASE_URL;
}

/**
 * Sets a custom backend API URL (useful for remote testing or different port).
 */
function setApiBaseUrl(url) {
  if (!url || url.trim() === "") {
    localStorage.removeItem("LMS_API_BASE_URL");
  } else {
    localStorage.setItem("LMS_API_BASE_URL", url.trim().replace(/\/+$/, ""));
  }
}

/**
 * Resets backend API Base URL to default.
 */
function resetApiBaseUrl() {
  localStorage.removeItem("LMS_API_BASE_URL");
}

/**
 * Tests connection to the Spring Boot backend server.
 */
async function checkBackendHealth() {
  const baseUrl = getApiBaseUrl();
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 4000);
    // Ping standard Spring Boot actuator or public endpoint
    const response = await fetch(`${baseUrl}/api/courses`, {
      method: "GET",
      signal: controller.signal,
      headers: { Accept: "application/json" }
    });
    clearTimeout(timeoutId);
    return {
      connected: true,
      status: response.status,
      url: baseUrl
    };
  } catch (err) {
    return {
      connected: false,
      error: err.message || "Connection refused",
      url: baseUrl
    };
  }
}

// Export for ES modules and attach to window for multi-page scripts
window.APP_CONFIG = {
  getApiBaseUrl,
  setApiBaseUrl,
  resetApiBaseUrl,
  checkBackendHealth,
  DEFAULT_API_BASE_URL
};
