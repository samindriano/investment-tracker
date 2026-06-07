package com.sam.finance.sahamlog.journal.dto;

public record ThesisSummaryResponse(
    long totalTheses,
    long activeTheses,
    long invalidatedTheses,
    long reviewsLast30Days) {
}
