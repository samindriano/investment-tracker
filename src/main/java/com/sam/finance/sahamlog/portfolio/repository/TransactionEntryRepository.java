package com.sam.finance.sahamlog.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long>, JpaSpecificationExecutor<TransactionEntry> {

    List<TransactionEntry> findByUser_IdOrderByStock_CodeAscTransactionDateAscIdAsc(Long userId);

    List<TransactionEntry> findByUser_IdAndStock_IdOrderByTransactionDateAscIdAsc(Long userId, Long stockId);

    Optional<TransactionEntry> findByIdAndUser_Id(Long id, Long userId);

    Page<TransactionEntry> findAllByUser_Id(Long userId, Pageable pageable);

    boolean existsByStock_Id(Long stockId);
}
