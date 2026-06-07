package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.portfolio.dto.TransactionFilter;
import com.sam.finance.sahamlog.portfolio.dto.TransactionRequest;
import com.sam.finance.sahamlog.portfolio.dto.TransactionResponse;
import com.sam.finance.sahamlog.portfolio.repository.TransactionEntryRepository;
import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionEntryRepository transactionEntryRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;
    private final PositionCalculator positionCalculator;

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Stock stock = stockService.findEntityById(request.stockId());

        if (request.type() == TransactionType.SELL) {
            validateSellPosition(userId, stock.getId(), request.quantityLot());
        }

        TransactionEntry entry = new TransactionEntry();
        entry.setUser(user);
        entry.setStock(stock);
        entry.setType(request.type());
        entry.setTransactionDate(request.transactionDate());
        entry.setQuantityLot(request.quantityLot());
        entry.setPrice(request.price());
        entry.setFee(request.fee());
        entry.setNotes(request.notes());

        return toResponse(transactionEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(TransactionFilter filter) {
        Long userId = currentUserService.getCurrentUser().id();
        Specification<TransactionEntry> specification = hasUserId(userId)
            .and(hasStockCode(filter.stockCode()))
            .and(hasType(filter.type()))
            .and(hasDateFrom(filter.dateFrom()))
            .and(hasDateTo(filter.dateTo()));

        return transactionEntryRepository.findAll(specification)
            .stream()
            .sorted((left, right) -> {
                int compareDate = right.getTransactionDate().compareTo(left.getTransactionDate());
                if (compareDate != 0) {
                    return compareDate;
                }
                return right.getId().compareTo(left.getId());
            })
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionEntry> findUserTransactionsOrdered(Long userId) {
        return transactionEntryRepository.findByUser_IdOrderByStock_CodeAscTransactionDateAscIdAsc(userId);
    }

    private void validateSellPosition(Long userId, Long stockId, Integer sellLot) {
        List<TransactionEntry> transactions = transactionEntryRepository.findByUser_IdAndStock_IdOrderByTransactionDateAscIdAsc(userId, stockId);
        Stock stock = stockService.findEntityById(stockId);
        PositionSnapshot position = positionCalculator.calculate(stock, transactions);
        if (sellLot > position.totalLot()) {
            throw new BusinessRuleViolationException("Sell quantity exceeds current holdings");
        }
    }

    private TransactionResponse toResponse(TransactionEntry entry) {
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

    private Specification<TransactionEntry> hasUserId(Long userId) {
        return (root, query, builder) -> builder.equal(root.join("user", JoinType.INNER).get("id"), userId);
    }

    private Specification<TransactionEntry> hasStockCode(String stockCode) {
        return (root, query, builder) -> {
            if (stockCode == null || stockCode.isBlank()) {
                return builder.conjunction();
            }
            return builder.equal(builder.upper(root.join("stock", JoinType.INNER).get("code")), stockCode.trim().toUpperCase());
        };
    }

    private Specification<TransactionEntry> hasType(TransactionType type) {
        return (root, query, builder) -> type == null ? builder.conjunction() : builder.equal(root.get("type"), type);
    }

    private Specification<TransactionEntry> hasDateFrom(java.time.LocalDate dateFrom) {
        return (root, query, builder) -> dateFrom == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom);
    }

    private Specification<TransactionEntry> hasDateTo(java.time.LocalDate dateTo) {
        return (root, query, builder) -> dateTo == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("transactionDate"), dateTo);
    }
}
