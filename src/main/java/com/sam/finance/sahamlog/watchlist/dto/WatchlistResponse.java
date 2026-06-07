package com.sam.finance.sahamlog.watchlist.dto;

import java.math.BigDecimal;

public record WatchlistResponse(
    Long id,
    Long stockId,
    String stockCode,
    String stockName,
    BigDecimal fairPrice,
    BigDecimal cheapPrice,
    BigDecimal veryCheapPrice,
    BigDecimal expensivePrice,
    String notes,
    BigDecimal currentPrice,
    ValuationZone valuationZone,
    BigDecimal premiumDiscountPercentage,
    BigDecimal marginOfSafetyPercentage) {
}
