package com.phep.casesvc.dto;

import jakarta.validation.constraints.NotBlank;

public record CaseDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String dob
) {}

