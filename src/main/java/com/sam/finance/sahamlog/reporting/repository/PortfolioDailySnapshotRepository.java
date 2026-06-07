package com.sam.finance.sahamlog.reporting.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.reporting.domain.PortfolioDailySnapshot;

public interface PortfolioDailySnapshotRepository extends JpaRepository<PortfolioDailySnapshot, Long> {

    Optional<PortfolioDailySnapshot> findByUser_IdAndSnapshotDate(Long userId, LocalDate snapshotDate);
}
