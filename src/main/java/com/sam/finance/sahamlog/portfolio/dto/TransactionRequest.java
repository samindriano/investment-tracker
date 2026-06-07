package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sam.finance.sahamlog.portfolio.domain.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
    @NotNull Long stockId,
    @NotNull TransactionType type,
    @NotNull LocalDate transactionDate,
    @NotNull @Positive Integer quantityLot,
    @NotNull @DecimalMin(value = "0.00") BigDecimal price,
    @NotNull @DecimalMin(value = "0.00") BigDecimal fee,
    @Size(max = 2000) String notes) {
}
