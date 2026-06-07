package com.sam.finance.sahamlog.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;

@Component
public class PositionCalculator {

    private static final int SHARES_PER_LOT = 100;
    private static final int SCALE = 2;

    public PositionSnapshot calculate(Stock stock, List<TransactionEntry> transactions) {
        int totalShares = 0;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (TransactionEntry transaction : transactions) {
            int transactionShares = transaction.getQuantityLot() * SHARES_PER_LOT;

            if (transaction.getType() == TransactionType.BUY) {
                BigDecimal transactionCost = transaction.getPrice()
                    .multiply(BigDecimal.valueOf(transactionShares))
                    .add(transaction.getFee());
                totalShares += transactionShares;
                totalCostBasis = totalCostBasis.add(transactionCost);
                continue;
            }

            if (transactionShares > totalShares) {
                throw new BusinessRuleViolationException("Sell quantity exceeds current holdings");
            }

            BigDecimal averagePrice = totalShares == 0
                ? BigDecimal.ZERO
                : totalCostBasis.divide(BigDecimal.valueOf(totalShares), 8, RoundingMode.HALF_UP);
            BigDecimal costToRemove = averagePrice.multiply(BigDecimal.valueOf(transactionShares));

            totalShares -= transactionShares;
            totalCostBasis = totalCostBasis.subtract(costToRemove);
            if (totalShares == 0) {
                totalCostBasis = BigDecimal.ZERO;
            }
        }

        int totalLot = totalShares / SHARES_PER_LOT;
        BigDecimal averagePrice = totalShares == 0
            ? BigDecimal.ZERO
            : totalCostBasis.divide(BigDecimal.valueOf(totalShares), SCALE, RoundingMode.HALF_UP);

        return new PositionSnapshot(
            stock,
            totalLot,
            totalShares,
            averagePrice,
            totalCostBasis.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public int sharesPerLot() {
        return SHARES_PER_LOT;
    }
}
