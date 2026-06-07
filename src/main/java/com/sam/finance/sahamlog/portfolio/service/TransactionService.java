package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        TransactionEntry entry = new TransactionEntry();
        entry.setUser(user);
        entry.setStock(stock);
        entry.setType(request.type());
        entry.setTransactionDate(request.transactionDate());
        entry.setQuantityLot(request.quantityLot());
        entry.setPrice(request.price());
        entry.setFee(request.fee());
        entry.setNotes(request.notes());

        validateCandidateState(userId, entry, null, false);
        return toResponse(transactionEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findPage(TransactionFilter filter, Pageable pageable) {
        Long userId = currentUserService.getCurrentUser().id();
        Specification<TransactionEntry> specification = hasUserId(userId)
            .and(hasStockCode(filter.stockCode()))
            .and(hasType(filter.type()))
            .and(hasDateFrom(filter.dateFrom()))
            .and(hasDateTo(filter.dateTo()));

        return transactionEntryRepository.findAll(specification, pageable)
            .map(this::toResponse);
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

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        TransactionEntry entry = findOwnedTransaction(id, userId);
        Long originalStockId = entry.getStock().getId();

        entry.setStock(stockService.findEntityById(request.stockId()));
        entry.setType(request.type());
        entry.setTransactionDate(request.transactionDate());
        entry.setQuantityLot(request.quantityLot());
        entry.setPrice(request.price());
        entry.setFee(request.fee());
        entry.setNotes(request.notes());

        validateCandidateState(userId, entry, originalStockId, false);
        return toResponse(transactionEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        TransactionEntry entry = findOwnedTransaction(id, userId);
        validateCandidateState(userId, entry, entry.getStock().getId(), true);
        transactionEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<TransactionEntry> findUserTransactionsOrdered(Long userId) {
        return transactionEntryRepository.findByUser_IdOrderByStock_CodeAscTransactionDateAscIdAsc(userId);
    }

    private TransactionEntry findOwnedTransaction(Long id, Long userId) {
        return transactionEntryRepository.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void validateCandidateState(Long userId, TransactionEntry candidate, Long originalStockId, boolean deleting) {
        java.util.LinkedHashSet<Long> affectedStockIds = new java.util.LinkedHashSet<>();
        if (originalStockId != null) {
            affectedStockIds.add(originalStockId);
        }
        affectedStockIds.add(candidate.getStock().getId());

        for (Long stockId : affectedStockIds) {
            List<TransactionEntry> transactions = transactionEntryRepository.findByUser_IdAndStock_IdOrderByTransactionDateAscIdAsc(userId, stockId)
                .stream()
                .filter(entry -> !Objects.equals(entry.getId(), candidate.getId()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

            if (!deleting && stockId.equals(candidate.getStock().getId())) {
                transactions.add(candidate);
            }

            transactions.sort((left, right) -> {
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
            });

            if (!transactions.isEmpty()) {
                Stock stock = stockService.findEntityById(stockId);
                positionCalculator.calculate(stock, transactions);
            }
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
