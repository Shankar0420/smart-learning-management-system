# Galgotias LMS — Environment Variables Specification

This document lists all environment variables used across the backend and frontend of Galgotias LMS, including their default values, security implications, and configuration examples.

---

## 1. Backend Environment Variables (Spring Boot)

All backend variables are resolved dynamically by `application.properties` and `application-prod.properties`.

| Variable Name | Required? | Default Value | Description & Security Guidelines |
|---|---|---|---|
| `PORT` | Optional | `8080` | Port on which the Spring Boot application listens. Automatically injected by hosting providers like Render via `$PORT`. |
| `SPRING_PROFILES_ACTIVE` | Optional | `prod` (in production) | Activates the production profile (`application-prod.properties`), enforcing `validate` DDL mode and strict production CORS. |
| `DB_URL` | **Yes** | *(None)* | JDBC connection URL to MariaDB/MySQL. For Aiven cloud SSL connections, use `?sslMode=verify-full&serverSslCert=/tmp/aiven-ca.pem` (the backend's `AivenSslInitializer` automatically extracts the bundled CA certificate from the JAR to `/tmp/aiven-ca.pem` on startup). |
| `DB_USERNAME` | **Yes** | *(None)* | Username for the MariaDB/MySQL database user (e.g. `avnadmin`). |
| `DB_PASSWORD` | **Yes** | *(None)* | Secure password for the database user. **NEVER hardcode or commit to version control.** |
| `DB_POOL_MAX` | Optional | `10` | Maximum number of concurrent connections in HikariCP pool. For cloud databases, keep between `5` and `10` to avoid exceeding connection quotas. |
| `DB_POOL_MIN` | Optional | `2` | Minimum number of idle connections maintained by HikariCP. |
| `JWT_SECRET` | **Yes** | *(None)* | Cryptographically secure secret key used for signing JWT access tokens (HMAC-SHA256). Must be at least 256 bits (64 hex characters). |
| `JWT_EXPIRATION_MS` | Optional | `86400000` | Expiration lifespan of issued JWT tokens in milliseconds. Default `86400000` = 24 hours. |
| `ADMIN_REGISTRATION_KEY` | **Yes** | *(None)* | Secret passphrase required in request payload to register an Admin account via `/api/auth/register-admin`. Prevents unauthorized admin privilege escalation. |
| `CORS_ALLOWED_ORIGINS` | Optional | `https://smart-learning-management-system.netlify.app` (prod) | Comma-separated list of origins permitted to make cross-origin requests. In production, restricted exclusively to the confirmed Netlify domain. |
| `HIBERNATE_DDL_AUTO` | Optional | `validate` (prod) / `update` (dev) | Hibernate schema behavior. In production (`application-prod.properties`), set to `validate` to ensure schema integrity without modifying existing records. |

---

## 2. Configuration Examples (.env format)

### A. Production Environment Configuration (Render Web Service)

Use these settings in the Render Dashboard under **Environment Variables**:

```bash
# Server Port (Render automatically sets $PORT, default is 8080)
PORT=8080

# Activate Production Profile
SPRING_PROFILES_ACTIVE=prod

# Aiven MariaDB / MySQL Cloud Database with Bundled SSL Certificate
DB_URL=jdbc:mariadb://mysql-your-project.aivencloud.com:25708/lms_db?sslMode=verify-full&serverSslCert=/tmp/aiven-ca.pem
DB_USERNAME=avnadmin
DB_PASSWORD=REPLACE_WITH_YOUR_DB_PASSWORD

# Connection Pool Tuning
DB_POOL_MAX=10
DB_POOL_MIN=2

# JWT Token Signing (Generate with: openssl rand -hex 32)
JWT_SECRET=REPLACE_WITH_YOUR_256_BIT_JWT_SECRET_HEX_KEY
JWT_EXPIRATION_MS=86400000

# Administrator Registration Passphrase
ADMIN_REGISTRATION_KEY=REPLACE_WITH_YOUR_ADMIN_REGISTRATION_KEY

# Production CORS (Strict: restricted exclusively to your confirmed Netlify frontend URL)
CORS_ALLOWED_ORIGINS=https://smart-learning-management-system.netlify.app

# Production DDL Safety (Schema validation only; never drops or alters tables)
HIBERNATE_DDL_AUTO=validate
```

### B. Local Development Configuration Example (`application.properties`)

When running locally for development:

```bash
PORT=8080
DB_URL=jdbc:mariadb://localhost:3306/lms_db
DB_USERNAME=lms_user
DB_PASSWORD=REPLACE_WITH_YOUR_LOCAL_DB_PASSWORD
JWT_SECRET=REPLACE_WITH_YOUR_LOCAL_JWT_SECRET
ADMIN_REGISTRATION_KEY=REPLACE_WITH_YOUR_LOCAL_ADMIN_KEY
CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*,https://smart-learning-management-system.netlify.app
```

---

## 3. Frontend Configuration (Client-side)

The frontend is vanilla HTML/CSS/JavaScript and does not expose secrets. Backend endpoint resolution is managed via `frontend/js/config.js`.

### Priority Hierarchy:
1. **Runtime Variable**: `window.__LMS_API_BASE_URL__` (can be injected before scripts run).
2. **Meta Tag**: `<meta name="lms-api-base-url" content="https://<backend>.onrender.com">`.
3. **User Storage**: `localStorage.getItem("LMS_API_BASE_URL")` (configurable via `APP_CONFIG.setApiBaseUrl(...)`).
4. **Localhost Fallback**: Defaults to `http://localhost:8080` when accessing through `localhost` or `127.0.0.1`.
5. **Default Production Fallback**: `DEFAULT_API_BASE_URL` defined in `config.js`.

---

## 4. How to Generate Production Secrets

### Generating JWT Secret Key (HMAC-SHA256)
Run in your local terminal:
```bash
openssl rand -hex 32
```
*Generates a 64-character cryptographic hex string (e.g. `REPLACE_WITH_YOUR_GENERATED_64_HEX_KEY`).*

### Generating Admin Registration Key
Run in your local terminal:
```bash
openssl rand -base64 24
```
*Generates a high-entropy string (e.g. `REPLACE_WITH_YOUR_GENERATED_BASE64_KEY`).*

---

## 5. Security Commandments

1. **NEVER commit `.env` or files containing plain passwords to GitHub.**
2. **Never expose `JWT_SECRET`, `DB_PASSWORD`, or `ADMIN_REGISTRATION_KEY` in frontend code.**
3. **In production, always use `HIBERNATE_DDL_AUTO=validate` instead of `create`, `create-drop`, or `update` to guarantee that existing user records, courses, and academic grades are permanently protected from automated DDL modification.**
4. **Production CORS must strictly allow only `https://smart-learning-management-system.netlify.app` with no wildcard subdomains.**
