package com.phep.casesvc.workflow;

import java.util.UUID;

public record IngestResult(
        UUID patientId,
        UUID caseId,
        UUID labResultId,
        boolean patientCreated,
        boolean caseCreated
) {}