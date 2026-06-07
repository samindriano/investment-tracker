package com.sam.finance.sahamlog.portfolio.service;

import org.springframework.stereotype.Component;

import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.dto.TransactionResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final PositionCalculator positionCalculator;

    public TransactionResponse toResponse(TransactionEntry entry) {
        return new TransactionResponse(
            entry.getId(),
            entry.getStock().getId(),
            entry.getStock().getCode(),
            entry.getStock().getName(),
            entry.getType(),
            entry.getTransactionDate(),
            entry.getQuantityLot(),
            entry.getQuantityLot() * positionCalculator.sharesPerLot(),
            entry.getPrice(),
            entry.getFee(),
            entry.getNotes());
    }
}
