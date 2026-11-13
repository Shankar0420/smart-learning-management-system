package com.lms.dto;

import com.lms.entity.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FacultyProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String employeeId;
    private String department;
    private String phone;
    private String designation;
    private String qualification;
    private String specialization;
    private LocalDate joiningDate;
    private String profilePhoto;
    private ApprovalStatus approvalStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private List<String> assignedCourses;
    private int totalAssignedCourses;
    private int totalUploadedMaterials;

    public FacultyProfileResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getAssignedCourses() { return assignedCourses; }
    public void setAssignedCourses(List<String> assignedCourses) { this.assignedCourses = assignedCourses; }

    public int getTotalAssignedCourses() { return totalAssignedCourses; }
    public void setTotalAssignedCourses(int totalAssignedCourses) { this.totalAssignedCourses = totalAssignedCourses; }

    public int getTotalUploadedMaterials() { return totalUploadedMaterials; }
    public void setTotalUploadedMaterials(int totalUploadedMaterials) { this.totalUploadedMaterials = totalUploadedMaterials; }
}

