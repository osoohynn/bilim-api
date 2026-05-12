package com.dgsw.bilimapi.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest (

    @NotBlank
    String refreshToken
) {}