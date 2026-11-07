/**
 * Galgotias LMS
 * Centralized API & Application Configuration
 */

// Default Spring Boot backend URL for production deployment
const DEFAULT_API_BASE_URL = "https://lms-backend-qkfy.onrender.com";

/**
 * Retrieves the active backend API Base URL.
 * Priority:
 * 1. window.__LMS_API_BASE_URL__ (runtime environment override)
 * 2. <meta name="lms-api-base-url" content="...">
 * 3. localStorage.getItem("LMS_API_BASE_URL")
 * 4. Localhost detection (http://localhost:8080)
 * 5. DEFAULT_API_BASE_URL (Render / Production backend)
 */
function getApiBaseUrl() {
    if (typeof window !== "undefined" && window.__LMS_API_BASE_URL__ && window.__LMS_API_BASE_URL__.trim() !== "") {
        return window.__LMS_API_BASE_URL__.trim().replace(/\/+$/, "");
    }

    if (typeof document !== "undefined") {
        const metaTag = document.querySelector('meta[name="lms-api-base-url"]');
        if (metaTag && metaTag.content && metaTag.content.trim() !== "") {
            return metaTag.content.trim().replace(/\/+$/, "");
        }
    }

    if (typeof localStorage !== "undefined") {
        const customUrl = localStorage.getItem("LMS_API_BASE_URL");
        if (customUrl && customUrl.trim() !== "") {
            return customUrl.trim().replace(/\/+$/, "");
        }
    }

    if (typeof window !== "undefined" && (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")) {
        return "http://localhost:8080";
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
 * Tests connection to the Spring Boot backend server via /api/health.
 */
async function checkBackendHealth() {
    const baseUrl = getApiBaseUrl();
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000);

        // Ping dedicated health endpoint
        let response = await fetch(`${baseUrl}/api/health`, {
            method: "GET",
            signal: controller.signal,
            headers: { Accept: "application/json" }
        }).catch(() => null);

        clearTimeout(timeoutId);

        // Fallback to /api/courses if health endpoint is unavailable
        if (!response) {
            const fallbackController = new AbortController();
            const fallbackTimeout = setTimeout(() => fallbackController.abort(), 4000);
            response = await fetch(`${baseUrl}/api/courses`, {
                method: "GET",
                signal: fallbackController.signal,
                headers: { Accept: "application/json" }
            });
            clearTimeout(fallbackTimeout);
        }

        return {
            connected: response.ok || response.status === 200,
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
