package com.sam.finance.sahamlog.dividend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendCalendarItemResponse(
    Long id,
    String stockCode,
    String stockName,
    LocalDate paymentDate,
    BigDecimal netReceived) {
}
