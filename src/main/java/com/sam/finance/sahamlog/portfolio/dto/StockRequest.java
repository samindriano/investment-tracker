package com.sam.finance.sahamlog.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StockRequest(
    @NotBlank @Size(max = 16) String code,
    @NotBlank @Size(max = 255) String name,
    @Size(max = 100) String sector) {
}
