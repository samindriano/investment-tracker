package com.sam.finance.sahamlog.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.portfolio.domain.StockPriceSnapshot;

public interface StockPriceSnapshotRepository extends JpaRepository<StockPriceSnapshot, Long> {

    List<StockPriceSnapshot> findByUser_IdOrderByStock_CodeAsc(Long userId);

    Optional<StockPriceSnapshot> findByUser_IdAndStock_Id(Long userId, Long stockId);

    boolean existsByStock_Id(Long stockId);
}
