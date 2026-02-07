package com.phep.casesvc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="lab_result")
@Getter @Setter
public class LabResultEntity extends CreatableEntity{

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private CaseEntity caseEntity; // nullable


    @Column(name = "test_code", nullable = false, length = 64)
    private String testCode;

    @Column(name = "result_value", length = 128)
    private String resultValue;

    @Column(name = "result_status", nullable = false, length = 32)
    private String resultStatus;

    @Column(name = "lab_name", length = 128)
    private String labName;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

}
