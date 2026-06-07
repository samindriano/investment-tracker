package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;
import java.util.List;

public record DividendSummaryResponse(
    int year,
    BigDecimal totalGrossDividend,
    BigDecimal totalTax,
    BigDecimal totalNetDividend,
    List<DividendStockSummaryResponse> byStock,
    List<DividendMonthSummaryResponse> byMonth) {
}
