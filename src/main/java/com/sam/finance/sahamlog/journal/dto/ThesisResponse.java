package com.sam.finance.sahamlog.journal.dto;

public record ThesisResponse(
    Long id,
    Long stockId,
    String stockCode,
    String stockName,
    String thesis,
    String risks,
    String invalidationCondition,
    String holdingPeriod,
    Short confidenceScore,
    String emotionTag) {
}
