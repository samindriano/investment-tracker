package com.sam.finance.sahamlog.portfolio.dto;

public record StockResponse(
    Long id,
    String code,
    String name,
    String sector) {
}
