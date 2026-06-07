package com.sam.finance.sahamlog.journal.service;

import org.springframework.stereotype.Component;

import com.sam.finance.sahamlog.journal.domain.InvestmentThesis;
import com.sam.finance.sahamlog.journal.domain.ThesisReview;
import com.sam.finance.sahamlog.journal.dto.ThesisResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisReviewResponse;

@Component
public class ThesisMapper {

    public ThesisResponse toResponse(InvestmentThesis thesis) {
        return new ThesisResponse(
            thesis.getId(),
            thesis.getStock().getId(),
            thesis.getStock().getCode(),
            thesis.getStock().getName(),
            thesis.getThesis(),
            thesis.getRisks(),
            thesis.getInvalidationCondition(),
            thesis.getHoldingPeriod(),
            thesis.getConfidenceScore(),
            thesis.getEmotionTag());
    }

    public ThesisReviewResponse toReviewResponse(ThesisReview review) {
        return new ThesisReviewResponse(
            review.getId(),
            review.getReviewDate(),
            review.getStillValid(),
            review.getAction(),
            review.getLesson());
    }
}
