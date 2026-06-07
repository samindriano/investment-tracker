package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
    BigDecimal totalModal,
    BigDecimal totalMarketValue,
    BigDecimal totalUnrealizedGainLoss,
    BigDecimal totalUnrealizedGainLossPercentage,
    List<DashboardHoldingResponse> holdings) {
}
