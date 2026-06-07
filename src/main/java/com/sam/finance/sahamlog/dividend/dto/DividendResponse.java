package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendResponse(
    Long id,
    Long stockId,
    String stockCode,
    String stockName,
    LocalDate cumDate,
    LocalDate paymentDate,
    BigDecimal dividendPerShare,
    Integer sharesOwned,
    BigDecimal taxRate,
    BigDecimal grossDividend,
    BigDecimal totalTax,
    BigDecimal netReceived) {
}
