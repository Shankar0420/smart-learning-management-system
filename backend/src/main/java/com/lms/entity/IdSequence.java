package com.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "id_sequences")
public class IdSequence {

    @Id
    @Column(length = 50)
    private String id; // e.g. "TEA_2026", "STU_2026"

    @Column(name = "current_val", nullable = false)
    private Long currentVal = 0L;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public IdSequence() {}

    public IdSequence(String id, Long currentVal) {
        this.id = id;
        this.currentVal = currentVal;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getCurrentVal() { return currentVal; }
    public void setCurrentVal(Long currentVal) { this.currentVal = currentVal; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

