package com.sam.finance.sahamlog.reporting.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.reporting.domain.ThesisStatusSnapshot;

public interface ThesisStatusSnapshotRepository extends JpaRepository<ThesisStatusSnapshot, Long> {

    Optional<ThesisStatusSnapshot> findByUser_IdAndSnapshotDate(Long userId, LocalDate snapshotDate);
}
