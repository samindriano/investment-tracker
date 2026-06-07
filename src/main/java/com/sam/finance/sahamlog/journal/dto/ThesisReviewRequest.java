package com.sam.finance.sahamlog.journal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThesisReviewRequest(
    @NotNull LocalDate reviewDate,
    @NotNull Boolean stillValid,
    @NotBlank @Size(max = 20) String action,
    @NotBlank String lesson) {
}
