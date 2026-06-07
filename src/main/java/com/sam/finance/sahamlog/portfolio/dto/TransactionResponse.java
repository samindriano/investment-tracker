package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sam.finance.sahamlog.portfolio.domain.TransactionType;

public record TransactionResponse(
    Long id,
    Long stockId,
    String stockCode,
    String stockName,
    TransactionType type,
    LocalDate transactionDate,
    Integer quantityLot,
    Integer quantityShare,
    BigDecimal price,
    BigDecimal fee,
    String notes) {
}
