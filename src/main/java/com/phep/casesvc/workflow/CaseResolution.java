package com.phep.casesvc.workflow;

import com.phep.casesvc.model.CaseEntity;

public record CaseResolution(
        CaseEntity caseEntity,
        boolean caseCreated
) {}