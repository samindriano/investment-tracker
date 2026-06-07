package com.sam.finance.sahamlog.journal.dto;

import java.time.LocalDate;

public record ThesisReviewResponse(
    Long id,
    LocalDate reviewDate,
    Boolean stillValid,
    String action,
    String lesson) {
}
