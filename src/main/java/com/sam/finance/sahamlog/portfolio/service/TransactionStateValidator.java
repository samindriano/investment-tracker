package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.repository.TransactionEntryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionStateValidator {

    private final TransactionEntryRepository transactionEntryRepository;
    private final StockService stockService;
    private final PositionCalculator positionCalculator;

    public void validateCandidateState(Long userId, TransactionEntry candidate, Long originalStockId, boolean deleting) {
        java.util.LinkedHashSet<Long> affectedStockIds = new java.util.LinkedHashSet<>();
        if (originalStockId != null) {
            affectedStockIds.add(originalStockId);
        }
        affectedStockIds.add(candidate.getStock().getId());

        for (Long stockId : affectedStockIds) {
            List<TransactionEntry> transactions = loadTransactionsWithoutCandidate(userId, stockId, candidate.getId());
            if (!deleting && stockId.equals(candidate.getStock().getId())) {
                transactions.add(candidate);
            }

            transactions.sort(this::compareChronologically);

            if (!transactions.isEmpty()) {
                Stock stock = stockService.findEntityById(stockId);
                positionCalculator.calculate(stock, transactions);
            }
        }
    }

    private List<TransactionEntry> loadTransactionsWithoutCandidate(Long userId, Long stockId, Long candidateId) {
        return transactionEntryRepository.findByUser_IdAndStock_IdOrderByTransactionDateAscIdAsc(userId, stockId)
            .stream()
            .filter(entry -> !Objects.equals(entry.getId(), candidateId))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    private int compareChronologically(TransactionEntry left, TransactionEntry right) {
        int compareDate = left.getTransactionDate().compareTo(right.getTransactionDate());
        if (compareDate != 0) {
            return compareDate;
        }

        Long leftId = left.getId();
        Long rightId = right.getId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return 1;
        }
        if (rightId == null) {
            return -1;
        }
        return leftId.compareTo(rightId);
    }
}
