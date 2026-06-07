package com.sam.finance.sahamlog.journal.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.journal.domain.ThesisReview;

public interface ThesisReviewRepository extends JpaRepository<ThesisReview, Long> {

    Page<ThesisReview> findByThesis_IdAndThesis_User_IdOrderByReviewDateDescIdDesc(Long thesisId, Long userId, Pageable pageable);

    long countDistinctByThesis_User_IdAndStillValidFalse(Long userId);

    long countByThesis_User_IdAndReviewDateGreaterThanEqual(Long userId, LocalDate reviewDate);
}
