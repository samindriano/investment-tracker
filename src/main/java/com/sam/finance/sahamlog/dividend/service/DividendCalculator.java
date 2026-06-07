package com.sam.finance.sahamlog.dividend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class DividendCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public BigDecimal grossDividend(BigDecimal dividendPerShare, int sharesOwned) {
        return dividendPerShare.multiply(BigDecimal.valueOf(sharesOwned)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal netReceived(BigDecimal dividendPerShare, int sharesOwned, BigDecimal taxRate) {
        BigDecimal gross = grossDividend(dividendPerShare, sharesOwned);
        BigDecimal taxMultiplier = BigDecimal.ONE.subtract(taxRate.divide(HUNDRED, 4, RoundingMode.HALF_UP));
        return gross.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal taxAmount(BigDecimal grossDividend, BigDecimal netReceived) {
        return grossDividend.subtract(netReceived).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal yieldOnCost(BigDecimal netDividend, BigDecimal costBasis) {
        if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netDividend.divide(costBasis, 4, RoundingMode.HALF_UP)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_UP);
    }
}
