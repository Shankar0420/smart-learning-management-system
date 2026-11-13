package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class AttendanceSessionRequest {

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotBlank(message = "Lecture topic is required")
    private String topic;

    private List<AttendanceRecordRequest> records;

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<AttendanceRecordRequest> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordRequest> records) {
        this.records = records;
    }
}

