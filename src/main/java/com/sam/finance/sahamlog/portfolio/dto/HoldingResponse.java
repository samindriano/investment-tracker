package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(
    Long stockId,
    String stockCode,
    String stockName,
    Integer totalLot,
    Integer totalShares,
    BigDecimal averagePrice,
    BigDecimal totalCostBasis) {
}
