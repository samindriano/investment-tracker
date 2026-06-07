package com.sam.finance.sahamlog.portfolio.service;

import java.math.BigDecimal;

import com.sam.finance.sahamlog.portfolio.domain.Stock;

public record PositionSnapshot(
    Stock stock,
    int totalLot,
    int totalShares,
    BigDecimal averagePrice,
    BigDecimal totalCostBasis) {
}
