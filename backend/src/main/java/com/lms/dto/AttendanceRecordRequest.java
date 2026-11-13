package com.lms.dto;

import com.lms.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class AttendanceRecordRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    private String remarks;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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
}

