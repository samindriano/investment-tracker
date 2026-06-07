package com.sam.finance.sahamlog.reporting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.reporting.domain.DividendMonthlySnapshot;

public interface DividendMonthlySnapshotRepository extends JpaRepository<DividendMonthlySnapshot, Long> {

    Optional<DividendMonthlySnapshot> findByUser_IdAndSnapshotYearAndSnapshotMonth(Long userId, Integer snapshotYear, Integer snapshotMonth);
}
