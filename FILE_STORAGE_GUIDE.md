# Galgotias LMS — File Storage & Ephemeral Cloud Handling Guide

This guide describes the file upload architecture of Galgotias LMS, explains how files are handled currently, and provides production solutions for persistent storage when deploying to free cloud hosting platforms.

---

## 1. Current File Storage Implementation

Galgotias LMS includes comprehensive file management for:
1. **Teacher Study Materials**: Lecture slides and reading material (`.pdf`, `.ppt`, `.pptx`).
2. **Assignment Attachments**: Question sheets and problem descriptions (`.pdf`, `.docx`, `.zip`, etc.).
3. **Student Submissions**: Completed student coursework and project files.

### Storage Architecture:
- Handled by: `com.lms.service.FileStorageService`
- Base Directory: `./uploads` (configurable via `file.upload-dir`)
- Subdirectories:
  - `./uploads/materials/`
  - `./uploads/assignments/`
  - `./uploads/submissions/`
- Unique Namespacing: Uses `UUID.randomUUID() + "_" + sanitizedFilename` to prevent file collisions and preserve original filenames.
- Validation:
  - Strict file extension whitelisting.
  - Strict 50MB file size ceiling.
  - Path traversal defense (`..` stripping via `StringUtils.cleanPath`).

---

## 2. Cloud Free Tier Limitation: Ephemeral Filesystems

> [!WARNING]
> **Ephemeral Disk Risk on Free Hosting**:
> Free cloud container platforms like **Render Free Tier**, **Koyeb Free Tier**, and **Fly.io** use **ephemeral filesystems**.
> This means:
> - Any file written to `./uploads/` exists only while the current container instance is running.
> - When the container sleeps due to inactivity or redeploys, all files written to local disk are wiped.
> - While file metadata (title, original filename, upload date) in the database remains intact, the underlying binary files in `./uploads/` will be lost unless persistent storage is used.

---

## 3. Recommended 100% Free Persistent Storage Solutions

To ensure study materials and student homework submissions are permanently preserved without monthly charges:

### Solution A: Cloudinary Free Tier (Easiest to Setup)
- **Free Quota**: 25GB managed storage / 25 Monthly Credits (~25,000 transformations or 25GB bandwidth).
- **Features**: Generates instant HTTPS CDN URLs; automatic image/document optimization.
- **Java SDK**: `com.cloudinary:cloudinary-http44:1.36.0`.

### Solution B: Supabase Storage / Backblaze B2 (S3-Compatible)
- **Supabase Storage**: 1GB free storage with full AWS S3-compatible API.
- **Backblaze B2**: 10GB free storage, zero egress fees when paired with Cloudflare.
- **Cloudflare R2**: 10GB free storage per month with zero egress bandwidth charges.

---

## 4. Migration Plan: Abstracting FileStorageService to Cloud / S3

When ready to switch from local disk to S3/Cloudinary, follow this simple 3-step abstraction pattern:

### Step 4.1: Define File Storage Interface
```java
package com.lms.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeMaterialFile(MultipartFile file);
    String storeAssignmentAttachment(MultipartFile file);
    String storeSubmissionFile(MultipartFile file);
    Resource loadFileAsResource(String filePath);
    void deleteFile(String filePath);
}
```

### Step 4.2: Implement S3 Storage Service
Add `software.amazon.awssdk:s3:2.20.+` to `pom.xml`, then create `S3StorageService.java`:
```java
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {
    // Stores directly to Supabase Storage, Backblaze B2, or Cloudflare R2 bucket
}
```

### Step 4.3: Toggle via Environment Variable
In `application.properties`:
```properties
storage.type=${STORAGE_TYPE:local}
# For S3:
# storage.type=s3
# storage.s3.bucket=galgotias-lms
# storage.s3.endpoint=https://<project-id>.supabase.co/storage/v1/s3
# storage.s3.access-key=...
# storage.s3.secret-key=...
```

---

## 5. File Security Best Practices

1. **Content-Type Validation**: Never rely solely on client-provided MIME headers; validate extensions against strictly allowed sets.
2. **File Size Hard Limits**: Configured in `application.properties`:
   ```properties
   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB
   ```
3. **Execution Prevention**: Upload directories must never be exposed as executable paths or scripts.
4. **Access Authorization**: Material and submission downloads are protected by Spring Security JWT role checks in `StudyMaterialController` and `AssignmentController`.

