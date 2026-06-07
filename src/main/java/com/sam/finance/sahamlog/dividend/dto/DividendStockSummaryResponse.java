package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;

public record DividendStockSummaryResponse(
    Long stockId,
    String stockCode,
    String stockName,
    BigDecimal totalGrossDividend,
    BigDecimal totalTax,
    BigDecimal totalNetDividend,
    BigDecimal yieldOnCostPercentage) {
}
