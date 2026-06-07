package com.sam.finance.sahamlog.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;

class PositionCalculatorTest {

    private PositionCalculator positionCalculator;
    private Stock stock;

    @BeforeEach
    void setUp() {
        positionCalculator = new PositionCalculator();
        stock = new Stock();
        stock.setId(1L);
        stock.setCode("BBCA");
        stock.setName("Bank Central Asia");
    }

    @Test
    void shouldCalculateWeightedAverageAfterMultipleBuys() {
        PositionSnapshot snapshot = positionCalculator.calculate(stock, List.of(
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 1), 10, "100.00", "10.00"),
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 2), 10, "200.00", "0.00")));

        assertThat(snapshot.totalLot()).isEqualTo(20);
        assertThat(snapshot.totalShares()).isEqualTo(2000);
        assertThat(snapshot.averagePrice()).isEqualByComparingTo("150.01");
        assertThat(snapshot.totalCostBasis()).isEqualByComparingTo("300010.00");
    }

    @Test
    void shouldKeepAveragePriceAfterPartialSell() {
        PositionSnapshot snapshot = positionCalculator.calculate(stock, List.of(
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 1), 10, "100.00", "0.00"),
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 2), 10, "200.00", "0.00"),
            transaction(TransactionType.SELL, LocalDate.of(2026, 1, 3), 5, "250.00", "5.00")));

        assertThat(snapshot.totalLot()).isEqualTo(15);
        assertThat(snapshot.totalShares()).isEqualTo(1500);
        assertThat(snapshot.averagePrice()).isEqualByComparingTo("150.00");
        assertThat(snapshot.totalCostBasis()).isEqualByComparingTo("225000.00");
    }

    @Test
    void shouldResetAveragePriceAfterFullExitAndReentry() {
        PositionSnapshot snapshot = positionCalculator.calculate(stock, List.of(
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 1), 10, "100.00", "0.00"),
            transaction(TransactionType.SELL, LocalDate.of(2026, 1, 2), 10, "110.00", "0.00"),
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 3), 5, "300.00", "0.00")));

        assertThat(snapshot.totalLot()).isEqualTo(5);
        assertThat(snapshot.totalShares()).isEqualTo(500);
        assertThat(snapshot.averagePrice()).isEqualByComparingTo("300.00");
        assertThat(snapshot.totalCostBasis()).isEqualByComparingTo("150000.00");
    }

    @Test
    void shouldRejectSellThatExceedsCurrentHoldings() {
        assertThatThrownBy(() -> positionCalculator.calculate(stock, List.of(
            transaction(TransactionType.BUY, LocalDate.of(2026, 1, 1), 1, "100.00", "0.00"),
            transaction(TransactionType.SELL, LocalDate.of(2026, 1, 2), 2, "100.00", "0.00"))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessage("Sell quantity exceeds current holdings");
    }

    private TransactionEntry transaction(
        TransactionType type,
        LocalDate date,
        int quantityLot,
        String price,
        String fee) {

        TransactionEntry transaction = new TransactionEntry();
        transaction.setStock(stock);
        transaction.setType(type);
        transaction.setTransactionDate(date);
        transaction.setQuantityLot(quantityLot);
        transaction.setPrice(new BigDecimal(price));
        transaction.setFee(new BigDecimal(fee));
        return transaction;
    }
}
