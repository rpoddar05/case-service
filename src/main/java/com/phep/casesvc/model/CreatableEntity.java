package com.phep.casesvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class CreatableEntity {

    @Column(name = "created_at", nullable = false)
    protected OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
