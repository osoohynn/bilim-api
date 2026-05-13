package com.dgsw.bilimapi.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChargePointRequest(@NotNull @Min(1) Integer amount) {
}
