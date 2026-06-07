package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PriceUpsertRequest(
    @NotNull @DecimalMin(value = "0.00") BigDecimal price,
    OffsetDateTime pricedAt) {
}
