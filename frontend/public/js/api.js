/**
 * Smart Learning Management System
 * Centralized API Communication Layer
 * Handles GET, POST, PUT, DELETE with Bearer Token and standardized HTTP status errors
 */

class ApiService {
  constructor() {
    this.getToken = () => {
      return localStorage.getItem("lms_token");
    };
  }

  /**
   * Helper to build standard headers with Bearer token
   */
  getHeaders(customHeaders = {}) {
    const headers = {
      "Content-Type": "application/json",
      "Accept": "application/json",
      ...customHeaders
    };

    const token = this.getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  /**
   * Core request handler
   */
  async request(endpoint, options = {}) {
    const baseUrl = window.APP_CONFIG.getApiBaseUrl();
    const cleanEndpoint = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
    const url = `${baseUrl}${cleanEndpoint}`;

    const config = {
      method: options.method || "GET",
      headers: this.getHeaders(options.headers),
      ...options
    };

    if (config.body && typeof config.body === "object" && !(config.body instanceof FormData)) {
      config.body = JSON.stringify(config.body);
    }

    try {
      const response = await fetch(url, config);
      return await this.handleResponse(response);
    } catch (error) {
      // Network error or failed connection to Spring Boot backend
      if (error.name === "TypeError" && error.message.includes("Failed to fetch")) {
        const errorMsg = `Unable to connect to Spring Boot backend at ${baseUrl}. Please ensure the Spring Boot application (lms_db) is running and CORS is enabled.`;
        throw new Error(errorMsg);
      }
      throw error;
    }
  }

  /**
   * Standardized HTTP Status Handling (200, 201, 400, 401, 403, 404, 409, 500)
   */
  async handleResponse(response) {
    const contentType = response.headers.get("content-type");
    let data = null;

    if (contentType && contentType.includes("application/json")) {
      try {
        data = await response.json();
      } catch (e) {
        data = null;
      }
    } else {
      try {
        data = await response.text();
      } catch (e) {
        data = null;
      }
    }

    if (response.ok) {
      return data;
    }

    // Extract cleanest user-friendly message returned by Spring Boot backend
    let message = "An unexpected error occurred.";
    if (data && typeof data === "object") {
      message = data.message || data.error || (data.errors ? Object.values(data.errors).join(", ") : message);
    } else if (typeof data === "string" && data.trim()) {
      message = data;
    }

    switch (response.status) {
      case 400:
        // Validation error
        throw new Error(message || "Invalid request. Please verify the submitted data.");
      case 401:
        // Unauthorized
        if (!window.location.pathname.includes("login.html") && !window.location.pathname.includes("register.html")) {
          // Token expired or invalid
          localStorage.removeItem("lms_token");
          localStorage.removeItem("lms_user");
          window.location.href = "/pages/login.html?expired=true";
        }
        throw new Error(message || "Invalid email or password. Please try again.");
      case 403:
        throw new Error("Access denied. You are not authorized to perform this action.");
      case 404:
        throw new Error(message || "The requested resource could not be found.");
      case 409:
        throw new Error(message || "Conflict occurred. You may already be enrolled or the resource already exists.");
      case 500:
        throw new Error("Internal server error. Please try again later or check backend logs.");
      default:
        throw new Error(message || `Request failed with status ${response.status}`);
    }
  }

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  }

  post(endpoint, body) {
    return this.request(endpoint, { method: "POST", body });
  }

  put(endpoint, body) {
    return this.request(endpoint, { method: "PUT", body });
  }

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  }
}

// Global API instance
window.api = new ApiService();
