package com.lms.dto;

import java.time.LocalDateTime;

/**
 * User response DTO — never includes password.
 */
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String employeeId;
    private String professionalId;
    private String enrollmentNumber;
    private String department;
    private String approvalStatus;
    private boolean active;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String name, String email, String role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UserResponse(Long id, String name, String email, String role, String employeeId, String approvalStatus, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.approvalStatus = approvalStatus;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UserResponse(Long id, String name, String email, String role, String employeeId, String professionalId, String enrollmentNumber, String department, String approvalStatus, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.professionalId = professionalId;
        this.enrollmentNumber = enrollmentNumber;
        this.department = department;
        this.approvalStatus = approvalStatus;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getProfessionalId() { return professionalId; }
    public void setProfessionalId(String professionalId) { this.professionalId = professionalId; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
