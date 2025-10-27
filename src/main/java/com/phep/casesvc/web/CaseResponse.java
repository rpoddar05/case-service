package com.phep.casesvc.web;

import java.time.OffsetDateTime;

public record CaseResponse(
        String caseId,
        String firstName,
        String lastName,
        String dob,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
