package com.lms.dto;

import com.lms.entity.AttendanceStatus;
import java.time.LocalDateTime;

public class AttendanceRecordResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String professionalId;
    private AttendanceStatus status;
    private String remarks;
    private LocalDateTime markedAt;

    public AttendanceRecordResponse() {
    }

    public AttendanceRecordResponse(Long id, Long studentId, String studentName, String studentEmail,
                                    String professionalId, AttendanceStatus status, String remarks, LocalDateTime markedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.professionalId = professionalId;
        this.status = status;
        this.remarks = remarks;
        this.markedAt = markedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}

