package com.phep.casesvc.workflow;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LabIngestCommand(
        String eventId,
        String caseId,              //  can be null
        String firstName,
        String lastName,
        LocalDate dob,
        String testCode,
        String resultValue,
        String resultStatus,
        String labName,
        OffsetDateTime receivedAt
) {
}
