package com.sam.finance.sahamlog.watchlist.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WatchlistRequest(
    @NotNull Long stockId,
    @NotNull @DecimalMin("0.00") BigDecimal fairPrice,
    @NotNull @DecimalMin("0.00") BigDecimal cheapPrice,
    @NotNull @DecimalMin("0.00") BigDecimal veryCheapPrice,
    @NotNull @DecimalMin("0.00") BigDecimal expensivePrice,
    @Size(max = 2000) String notes) {
}
