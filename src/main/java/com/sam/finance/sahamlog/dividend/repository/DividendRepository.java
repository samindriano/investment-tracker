package com.sam.finance.sahamlog.dividend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.dividend.domain.Dividend;

public interface DividendRepository extends JpaRepository<Dividend, Long> {

    Page<Dividend> findByUser_IdAndStock_CodeContainingIgnoreCaseAndPaymentDateBetween(
        Long userId,
        String stockCode,
        LocalDate from,
        LocalDate to,
        Pageable pageable);

    Page<Dividend> findByUser_IdAndPaymentDateBetween(Long userId, LocalDate from, LocalDate to, Pageable pageable);

    List<Dividend> findByUser_IdAndPaymentDateBetweenOrderByPaymentDateAsc(Long userId, LocalDate from, LocalDate to);

    boolean existsByStock_Id(Long stockId);
}
