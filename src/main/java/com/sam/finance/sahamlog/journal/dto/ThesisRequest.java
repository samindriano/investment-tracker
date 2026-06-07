package com.sam.finance.sahamlog.journal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThesisRequest(
    @NotNull Long stockId,
    @NotBlank String thesis,
    String risks,
    String invalidationCondition,
    @Size(max = 50) String holdingPeriod,
    @Min(0) @Max(10) Short confidenceScore,
    @Size(max = 50) String emotionTag) {
}
