package com.sam.finance.sahamlog.portfolio.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PriceResponse(
    Long stockId,
    String stockCode,
    String stockName,
    BigDecimal price,
    OffsetDateTime pricedAt) {
}
