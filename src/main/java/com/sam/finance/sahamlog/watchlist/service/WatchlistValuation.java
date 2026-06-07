package com.sam.finance.sahamlog.watchlist.service;

import java.math.BigDecimal;

import com.sam.finance.sahamlog.watchlist.dto.ValuationZone;

public record WatchlistValuation(
    ValuationZone zone,
    BigDecimal premiumDiscountPercentage,
    BigDecimal marginOfSafetyPercentage) {
}
