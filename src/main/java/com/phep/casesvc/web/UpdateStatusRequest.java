package com.phep.casesvc.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank String status
) { }
