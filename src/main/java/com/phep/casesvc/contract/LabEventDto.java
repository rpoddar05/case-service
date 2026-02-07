package com.phep.casesvc.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true) // consumer should be tolerant
public record LabEventDto(
        String caseId,
        String patientFirstName,
        String patientLastName,
        LocalDate dob,
        String testCode,
        String resultValue,
        String resultStatus,  // keep String for now
        String labName
) {}
