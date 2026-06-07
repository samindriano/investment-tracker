package com.sam.finance.sahamlog.portfolio.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;

import jakarta.persistence.criteria.JoinType;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<TransactionEntry> hasUserId(Long userId) {
        return (root, query, builder) -> builder.equal(root.join("user", JoinType.INNER).get("id"), userId);
    }

    public static Specification<TransactionEntry> hasStockCode(String stockCode) {
        return (root, query, builder) -> {
            if (stockCode == null || stockCode.isBlank()) {
                return builder.conjunction();
            }
            return builder.equal(builder.upper(root.join("stock", JoinType.INNER).get("code")), stockCode.trim().toUpperCase());
        };
    }

    public static Specification<TransactionEntry> hasType(TransactionType type) {
        return (root, query, builder) -> type == null ? builder.conjunction() : builder.equal(root.get("type"), type);
    }

    public static Specification<TransactionEntry> hasDateFrom(LocalDate dateFrom) {
        return (root, query, builder) -> dateFrom == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom);
    }

    public static Specification<TransactionEntry> hasDateTo(LocalDate dateTo) {
        return (root, query, builder) -> dateTo == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("transactionDate"), dateTo);
    }
}
