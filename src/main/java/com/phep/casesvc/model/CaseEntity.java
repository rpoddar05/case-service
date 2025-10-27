package com.phep.casesvc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="cases")
@Getter
@Setter
public class CaseEntity {

    @Id
    @Column(name="case_id", nullable = false, updatable = false)
    private String caseId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String dob;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private CaseStatus status = CaseStatus.OPEN;


    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt =OffsetDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt =OffsetDateTime.now();
}

