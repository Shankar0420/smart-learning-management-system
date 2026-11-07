# Galgotias LMS — Production Deployment Guide

This guide details the complete, step-by-step procedure to deploy the Galgotias Learning Management System to 100% free hosting tiers without breaking any existing functionality.

---

## Architecture Overview

```
                      +---------------------------------------+
                      |               CLIENT                  |
                      |  (Desktop / Tablet / Mobile Browser)  |
                      +---------------------------------------+
                                          |
                      +-------------------+-------------------+
                      |                                       |
                      v                                       v
         +--------------------------+            +--------------------------+
         |    FRONTEND HOSTING      |            |     BACKEND HOSTING      |
         |         Netlify          |            |     Render / Koyeb       |
         |   (Static HTML/CSS/JS)   |            |   (Spring Boot 3.2.5)    |
         |  Base Dir: `frontend`    |            |   Java 17 / Port $PORT   |
         |  Publish: `.`            |            |   Endpoints: `/api/*`    |
         +--------------------------+            +--------------------------+
                                                              |
                                                              v
                                                 +--------------------------+
                                                 |     DATABASE HOSTING     |
                                                 |    Aiven / TiDB Cloud    |
                                                 |   (Free MariaDB/MySQL)   |
                                                 |   SSL Connection (TLS)   |
                                                 +--------------------------+
```

---

## 1. Database Setup (Free Tier)

Recommended Free Databases:
- **Aiven for MySQL/MariaDB**: 5GB SSD, 1GB RAM, automated TLS/SSL.
- **TiDB Cloud Serverless**: 25GB free MySQL-compatible cloud database.

### Step 1.1: Create Database Instance
1. Sign up on [Aiven.io](https://aiven.io/) or [TiDB Cloud](https://tidbcloud.com/).
2. Create a free MySQL/MariaDB service named `galgotias-lms-db`.
3. Save the connection details:
   - Host
   - Port (e.g., 3306 or Aiven port like 12345)
   - Database name (e.g., `defaultdb` or `lms`)
   - Username (e.g., `avnadmin`)
   - Password
   - CA Certificate (`ca.pem` if required)

### Step 1.2: Execute Database Migrations
Refer to `DATABASE_MIGRATION_GUIDE.md` for the exact sequence.
Execute in order:
1. `backend/database/schema.sql` (Initial baseline tables)
2. `backend/database/migrations/V2__add_professional_ids_and_approval.sql` (Phase 2 enhancements)
3. `backend/database/migrations/V3__add_study_materials_and_password_reset.sql` (Phase 3 enhancements)

> [!IMPORTANT]
> All migration scripts use `IF NOT EXISTS` and check `INFORMATION_SCHEMA`. They are 100% non-destructive and will never delete or reset existing data.

---

## 2. Backend Deployment (Render Free Web Service)

Render provides a free Web Service tier supporting Java applications.

### Step 2.1: Create Web Service on Render
1. Sign in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** -> **Web Service**.
3. Connect your GitHub repository containing the Galgotias LMS project.
4. Configure service settings:
   - **Name**: `galgotias-lms-backend` (or custom name)
   - **Region**: Closest to your users (e.g., Singapore / Frankfurt / Oregon)
   - **Branch**: `main` (or your production deployment branch)
   - **Root Directory**: `backend`
   - **Environment**: `Java` (or `Docker`)
   - **Build Command**:
     ```bash
     mvn clean package -DskipTests
     ```
   - **Start Command**:
     ```bash
     java -Dspring.profiles.active=prod -Dserver.port=$PORT -jar target/lms-backend-1.0.0.jar
     ```
   - **Instance Type**: `Free` (0.1 CPU, 512MB RAM)

### Step 2.2: Configure Backend Environment Variables
In Render Dashboard -> **Environment**, add the following keys:

| Key | Example Value | Description |
|-----|---------------|-------------|
| `PORT` | `8080` (or leave to Render) | Server listening port |
| `DB_URL` | `jdbc:mariadb://<host>:<port>/<dbname>?sslMode=verify-ca` | JDBC connection string |
| `DB_USERNAME` | `<db_username>` | Database username |
| `DB_PASSWORD` | `<db_password>` | Database password |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | 256-bit Hex secret key |
| `JWT_EXPIRATION_MS` | `86400000` | 24 hours in milliseconds |
| `ADMIN_REGISTRATION_KEY` | `<your-secure-admin-passphrase>` | Secret key for registering admins |
| `CORS_ALLOWED_ORIGINS` | `https://*.netlify.app,https://<your-custom-name>.netlify.app` | Allowed frontend origins |
| `HIBERNATE_DDL_AUTO` | `validate` | Validates schema without mutating DB |
| `DB_POOL_MAX` | `10` | Max HikariCP connections |
| `DB_POOL_MIN` | `2` | Min idle connections |

### Step 2.3: Verify Backend Deployment
Once the build completes and service shows **Live**:
1. Open `https://<your-render-app>.onrender.com/api/health` in your browser.
2. Expected JSON response:
   ```json
   {
     "status": "UP",
     "service": "Galgotias LMS API",
     "timestamp": 1731000000000
   }
   ```
3. Test public courses endpoint: `https://<your-render-app>.onrender.com/api/courses`.

> [!NOTE]
> **Free Tier Sleep Behavior**: Free services on Render spin down after 15 minutes of inactivity. When a new request arrives, it takes approximately 45–60 seconds to wake up (cold start). The frontend includes a live backend status indicator to notify users while the server is waking up.

---

## 3. Frontend Deployment (Netlify Free Tier)

Netlify serves static HTML, CSS, and Vanilla JavaScript with global CDN distribution.

### Step 3.1: Link Repository on Netlify
1. Log in to [Netlify](https://app.netlify.com/).
2. Click **Add new site** -> **Import an existing project**.
3. Choose **GitHub** and select the LMS repository.
4. Set Build Settings:
   - **Base directory**: `frontend`
   - **Build command**: *(leave blank or `npm run build` if bundling with Vite)*
   - **Publish directory**: `.` (or `dist` if using Vite build)
5. Click **Deploy Site**.

### Step 3.2: Connect Frontend to Backend URL
There are two ways to connect your Netlify frontend to your live backend:

#### Method A: Setting Custom Base URL in Browser (Instant, No Rebuild)
1. Open your Netlify site URL.
2. Open Browser Developer Console (`F12` -> Console).
3. Run:
   ```javascript
   APP_CONFIG.setApiBaseUrl("https://<your-render-app>.onrender.com");
   ```
4. Refresh the page. The system will permanently remember the URL in `localStorage`.

#### Method B: Set Default API URL in Code
1. In `frontend/js/config.js`, update `DEFAULT_API_BASE_URL`:
   ```javascript
   const DEFAULT_API_BASE_URL = "https://<your-render-app>.onrender.com";
   ```
2. Commit and push. Netlify will auto-deploy the change.

---

## 4. Automated CI/CD Pipeline (GitHub Actions)

The repository includes an automated pipeline in `.github/workflows/deploy.yml` configured exclusively for the `upgrade/phase1-error-fixing` branch.

### Pipeline Architecture & Quality Gates

```
[Push to upgrade/phase1-error-fixing]
                |
                v
[Job 1: test-and-verify]
  - Java 17 + Maven 57 Integration Tests (Isolated H2 In-Memory DB)
  - Backend JAR Packaging Pre-verification
  - Frontend JS Syntax Validation (node --check)
  - Critical Assets Check (index.html, config.js, dashboards)
                |
          (Passed 100%?)
         /              \
       NO                YES
       /                  \
[Pipeline Fails]      [Job 2: deploy-backend]
[Zero Deploys]          - Trigger Render Deploy Hook (POST)
                           |
                      [Job 3: deploy-frontend]
                        - Trigger Netlify Build Hook (POST)
                           |
                      [Job 4: verify-and-report]
                        - Poll /api/health endpoint
                        - Verify Netlify CDN reachability
                        - Publish Markdown Summary
```

### Required GitHub Encrypted Secrets

Navigate to **GitHub Repository $\rightarrow$ Settings $\rightarrow$ Secrets and variables $\rightarrow$ Actions $\rightarrow$ New repository secret**:

| Secret Name | Service | How to Obtain | Permissions |
|---|---|---|---|
| `RENDER_DEPLOY_HOOK_URL` | Render | Render Dashboard $\rightarrow$ Web Service $\rightarrow$ **Settings** $\rightarrow$ scroll down to **Deploy Hook** $\rightarrow$ Copy URL | Webhook only (can only trigger redeploy for this specific service; cannot access credentials or database) |
| `NETLIFY_BUILD_HOOK_URL` | Netlify | Netlify Dashboard $\rightarrow$ Site configuration $\rightarrow$ **Build & deploy** $\rightarrow$ **Continuous deployment** $\rightarrow$ **Build hooks** $\rightarrow$ Add build hook $\rightarrow$ Copy URL | Webhook only (can only trigger build for this specific site) |

### Preventing Auto-Deploy Bypass on Hosting Services

To guarantee that code is **never deployed before passing all 57 tests in GitHub Actions**, update these hosting settings:

1. **Render Dashboard**:
   - Go to **LMS Backend Web Service $\rightarrow$ Settings $\rightarrow$ Build & Deploy**.
   - Set **Auto-Deploy** to **No**.
   - Render will now deploy *only* when triggered by the GitHub Actions `RENDER_DEPLOY_HOOK_URL`.
2. **Netlify Dashboard**:
   - Go to **Site configuration $\rightarrow$ Build & deploy $\rightarrow$ Continuous deployment $\rightarrow$ Builds**.
   - Under **Build status**, select **Stop builds** (or lock deploys).
   - Netlify will now build *only* when triggered by the GitHub Actions `NETLIFY_BUILD_HOOK_URL`.

---

## 5. Alternative Free Backend Hosting Options

If you prefer alternatives to Render:

### Option B: Koyeb (Zero Cold Start on Free Tier)
1. Sign up on [Koyeb.com](https://www.koyeb.com/).
2. Create an App -> Deploy via GitHub.
3. Select `backend/` directory, set Dockerfile or Maven buildpack.
4. Add environment variables identically to Section 2.2.
5. Free tier includes 1 nano service running continuously.

### Option C: Fly.io
1. Install `flyctl`.
2. Launch backend with `fly launch --path ./backend`.
3. Set secrets with `fly secrets set DB_URL=... DB_USERNAME=... JWT_SECRET=...`.

---

## 6. Post-Deployment Verification Checklist

- [ ] `GET https://<backend>/api/health` returns `{"status":"UP", ...}`.
- [ ] Primary Admin can log in using existing credentials.
- [ ] Teacher registration works -> Status is `PENDING`.
- [ ] Admin dashboard lists pending teacher -> Can approve with ID `GUL-TEA-2026-XXX`.
- [ ] Approved teacher can log in and access `/teacher/dashboard.html`.
- [ ] Teacher can upload PDF/PPT study material.
- [ ] Student can enroll and view uploaded study material.
- [ ] Student can take quiz and view score.
- [ ] Teacher can create assignment and student can submit.
- [ ] Admin can deactivate user -> Deactivated user's JWT is immediately rejected.
