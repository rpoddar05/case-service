package com.phep.casesvc.model;

public enum CaseStatus {
    OPEN,
    CLOSED,
    IN_PROGRESS;

    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS;
    }
}
