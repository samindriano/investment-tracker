package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DividendRequest(
    @NotNull Long stockId,
    LocalDate cumDate,
    @NotNull LocalDate paymentDate,
    @NotNull @DecimalMin("0.00") BigDecimal dividendPerShare,
    @NotNull @Positive Integer sharesOwned,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate) {
}
