package com.dgsw.bilimapi.domain.reading.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(@NotNull Long bookId) {}
