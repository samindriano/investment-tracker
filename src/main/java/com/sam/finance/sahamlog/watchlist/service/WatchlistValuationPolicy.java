package com.sam.finance.sahamlog.watchlist.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.sam.finance.sahamlog.watchlist.domain.WatchlistItem;
import com.sam.finance.sahamlog.watchlist.dto.ValuationZone;

@Component
public class WatchlistValuationPolicy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public WatchlistValuation evaluate(WatchlistItem item, BigDecimal currentPrice) {
        if (currentPrice == null) {
            return new WatchlistValuation(ValuationZone.NO_PRICE, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal premiumDiscount = currentPrice.subtract(item.getFairPrice())
            .divide(item.getFairPrice(), 4, RoundingMode.HALF_UP)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal marginOfSafety = item.getFairPrice().subtract(currentPrice)
            .divide(item.getFairPrice(), 4, RoundingMode.HALF_UP)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_UP);

        return new WatchlistValuation(resolveZone(item, currentPrice), premiumDiscount, marginOfSafety);
    }

    private ValuationZone resolveZone(WatchlistItem item, BigDecimal currentPrice) {
        if (currentPrice.compareTo(item.getVeryCheapPrice()) <= 0) {
            return ValuationZone.VERY_CHEAP;
        }
        if (currentPrice.compareTo(item.getCheapPrice()) <= 0) {
            return ValuationZone.CHEAP;
        }
        if (currentPrice.compareTo(item.getFairPrice()) <= 0) {
            return ValuationZone.FAIR;
        }
        if (currentPrice.compareTo(item.getExpensivePrice()) <= 0) {
            return ValuationZone.EXPENSIVE;
        }
        return ValuationZone.OVERPRICED;
    }
}
