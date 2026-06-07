package com.sam.finance.sahamlog.portfolio.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.dto.HoldingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final CurrentUserService currentUserService;
    private final TransactionService transactionService;
    private final PositionCalculator positionCalculator;

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings() {
        Long userId = currentUserService.getCurrentUser().id();
        List<PositionSnapshot> positions = calculatePositions(userId);

        return positions.stream()
            .map(position -> new HoldingResponse(
                position.stock().getId(),
                position.stock().getCode(),
                position.stock().getName(),
                position.totalLot(),
                position.totalShares(),
                position.averagePrice(),
                position.totalCostBasis()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PositionSnapshot> calculatePositions(Long userId) {
        List<TransactionEntry> transactions = transactionService.findUserTransactionsOrdered(userId);
        Map<Long, List<TransactionEntry>> transactionsByStock = new LinkedHashMap<>();

        for (TransactionEntry transaction : transactions) {
            transactionsByStock.computeIfAbsent(transaction.getStock().getId(), ignored -> new ArrayList<>()).add(transaction);
        }

        return transactionsByStock.values()
            .stream()
            .map(entries -> positionCalculator.calculate(entries.getFirst().getStock(), entries))
            .filter(position -> position.totalShares() > 0)
            .toList();
    }
}
