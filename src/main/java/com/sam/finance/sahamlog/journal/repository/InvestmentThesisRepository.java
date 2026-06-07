package com.sam.finance.sahamlog.journal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.journal.domain.InvestmentThesis;

public interface InvestmentThesisRepository extends JpaRepository<InvestmentThesis, Long> {

    List<InvestmentThesis> findByUser_IdOrderByStock_CodeAsc(Long userId);

    Optional<InvestmentThesis> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndStock_Id(Long userId, Long stockId);

    boolean existsByStock_Id(Long stockId);

    long countByUser_Id(Long userId);
}
