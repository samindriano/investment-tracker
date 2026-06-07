package com.sam.finance.sahamlog.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.StockPriceSnapshot;
import com.sam.finance.sahamlog.portfolio.dto.DashboardHoldingResponse;
import com.sam.finance.sahamlog.portfolio.dto.DashboardSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CurrentUserService currentUserService;
    private final PortfolioService portfolioService;
    private final StockPriceService stockPriceService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return getSummaryForUser(currentUserService.getCurrentUser().id());
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummaryForUser(Long userId) {
        List<PositionSnapshot> positions = portfolioService.calculatePositions(userId);
        Map<Long, StockPriceSnapshot> pricesByStockId = stockPriceService.findSnapshotMapByUserId(userId);

        BigDecimal totalModal = positions.stream()
            .map(PositionSnapshot::totalCostBasis)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        List<DashboardHoldingResponse> holdings = new ArrayList<>();
        BigDecimal totalMarketValue = BigDecimal.ZERO;

        for (PositionSnapshot position : positions) {
            StockPriceSnapshot priceSnapshot = pricesByStockId.get(position.stock().getId());
            BigDecimal currentPrice = priceSnapshot == null ? null : priceSnapshot.getPrice();
            BigDecimal marketValue = currentPrice == null
                ? BigDecimal.ZERO
                : currentPrice.multiply(BigDecimal.valueOf(position.totalShares())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedGainLoss = marketValue.subtract(position.totalCostBasis()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedGainLossPercentage = position.totalCostBasis().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : unrealizedGainLoss.divide(position.totalCostBasis(), 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);

            totalMarketValue = totalMarketValue.add(marketValue);

            holdings.add(new DashboardHoldingResponse(
                position.stock().getId(),
                position.stock().getCode(),
                position.stock().getName(),
                position.totalLot(),
                position.totalShares(),
                position.averagePrice(),
                position.totalCostBasis(),
                currentPrice,
                marketValue,
                unrealizedGainLoss,
                unrealizedGainLossPercentage,
                BigDecimal.ZERO));
        }

        BigDecimal totalUnrealizedGainLoss = totalMarketValue.subtract(totalModal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalUnrealizedGainLossPercentage = totalModal.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalUnrealizedGainLoss.divide(totalModal, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotalMarketValue = totalMarketValue;

        List<DashboardHoldingResponse> holdingsWithAllocation = holdings.stream()
            .map(holding -> new DashboardHoldingResponse(
                holding.stockId(),
                holding.stockCode(),
                holding.stockName(),
                holding.totalLot(),
                holding.totalShares(),
                holding.averagePrice(),
                holding.totalCostBasis(),
                holding.currentPrice(),
                holding.marketValue(),
                holding.unrealizedGainLoss(),
                holding.unrealizedGainLossPercentage(),
                finalTotalMarketValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : holding.marketValue().divide(finalTotalMarketValue, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP)))
            .toList();

        return new DashboardSummaryResponse(
            totalModal,
            totalMarketValue.setScale(2, RoundingMode.HALF_UP),
            totalUnrealizedGainLoss,
            totalUnrealizedGainLossPercentage,
            holdingsWithAllocation);
    }
}
