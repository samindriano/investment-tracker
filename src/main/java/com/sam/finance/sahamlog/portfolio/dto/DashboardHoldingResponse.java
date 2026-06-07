package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;

public record DashboardHoldingResponse(
    Long stockId,
    String stockCode,
    String stockName,
    Integer totalLot,
    Integer totalShares,
    BigDecimal averagePrice,
    BigDecimal totalCostBasis,
    BigDecimal currentPrice,
    BigDecimal marketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercentage,
    BigDecimal allocationPercentage) {
}
