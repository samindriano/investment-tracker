package com.sam.finance.sahamlog.portfolio.dto;

import java.time.LocalDate;

import com.sam.finance.sahamlog.portfolio.domain.TransactionType;

public record TransactionFilter(
    String stockCode,
    TransactionType type,
    LocalDate dateFrom,
    LocalDate dateTo) {
}
