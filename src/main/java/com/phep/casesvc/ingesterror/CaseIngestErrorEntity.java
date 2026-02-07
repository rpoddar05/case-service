package com.phep.casesvc.ingesterror;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "case_ingest_error")
public class CaseIngestErrorEntity {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private String topic;

    @Column(name = "partition", nullable = false)
    private int partition;

    @Column(name = "kafka_offset", nullable = false)
    private long offset;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "error_message", nullable = false, columnDefinition = "text")
    private String errorMessage;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private String status;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "NEW";
    }
}
