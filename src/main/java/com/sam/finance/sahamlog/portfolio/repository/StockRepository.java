package com.sam.finance.sahamlog.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.portfolio.domain.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Stock> findByCodeIgnoreCase(String code);

    Optional<Stock> findById(Long id);
}
