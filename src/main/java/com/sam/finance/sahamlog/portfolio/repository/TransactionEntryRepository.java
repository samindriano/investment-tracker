package com.sam.finance.sahamlog.portfolio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long>, JpaSpecificationExecutor<TransactionEntry> {

    List<TransactionEntry> findByUser_IdOrderByStock_CodeAscTransactionDateAscIdAsc(Long userId);

    List<TransactionEntry> findByUser_IdAndStock_IdOrderByTransactionDateAscIdAsc(Long userId, Long stockId);
}
