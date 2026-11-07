package com.lms.service;

import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path baseStorageLocation;

    private static final Set<String> ALLOWED_MATERIAL_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "ppt", "pptx")
    );

    private static final Set<String> ALLOWED_ASSIGNMENT_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "txt", "zip", "png", "jpg", "jpeg")
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.baseStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseStorageLocation);
            Files.createDirectories(this.baseStorageLocation.resolve("materials"));
            Files.createDirectories(this.baseStorageLocation.resolve("assignments"));
            Files.createDirectories(this.baseStorageLocation.resolve("submissions"));
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload storage directories", ex);
        }
    }

    public String storeMaterialFile(MultipartFile file) {
        return storeFile(file, "materials", ALLOWED_MATERIAL_EXTENSIONS);
    }

    public String storeAssignmentAttachment(MultipartFile file) {
        return storeFile(file, "assignments", ALLOWED_ASSIGNMENT_EXTENSIONS);
    }

    public String storeSubmissionFile(MultipartFile file) {
        return storeFile(file, "submissions", ALLOWED_ASSIGNMENT_EXTENSIONS);
    }

    private String storeFile(MultipartFile file, String subDirectory, Set<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 50MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");

        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename with path traversal sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("File type '." + extension + "' is not supported. Allowed extensions: " + String.join(", ", allowedExtensions));
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;

        try {
            Path targetLocation = this.baseStorageLocation.resolve(subDirectory).resolve(uniqueFileName).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String subDirectory, String fileName) {
        try {
            Path filePath = this.baseStorageLocation.resolve(subDirectory).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path is invalid: " + fileName);
        }
    }

    public void deleteFile(String subDirectory, String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            Path filePath = this.baseStorageLocation.resolve(subDirectory).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best effort deletion
        }
    }

    public Path getFullPath(String subDirectory, String fileName) {
        return this.baseStorageLocation.resolve(subDirectory).resolve(fileName).normalize();
    }

    public String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }
}
