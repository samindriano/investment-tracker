package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;

public record DividendMonthSummaryResponse(
    int month,
    BigDecimal totalGrossDividend,
    BigDecimal totalTax,
    BigDecimal totalNetDividend) {
}
