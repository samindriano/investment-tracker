package com.sam.finance.sahamlog.journal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.journal.domain.InvestmentThesis;
import com.sam.finance.sahamlog.journal.domain.ThesisReview;
import com.sam.finance.sahamlog.journal.dto.ThesisRequest;
import com.sam.finance.sahamlog.journal.dto.ThesisResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisReviewRequest;
import com.sam.finance.sahamlog.journal.dto.ThesisReviewResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.journal.repository.InvestmentThesisRepository;
import com.sam.finance.sahamlog.journal.repository.ThesisReviewRepository;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.service.StockService;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThesisService {

    private final InvestmentThesisRepository investmentThesisRepository;
    private final ThesisReviewRepository thesisReviewRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;

    @Transactional
    public ThesisResponse create(ThesisRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        if (investmentThesisRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("A thesis already exists for this stock");
        }

        InvestmentThesis thesis = new InvestmentThesis();
        applyRequest(thesis, request, userId);
        return toResponse(investmentThesisRepository.save(thesis));
    }

    @Transactional(readOnly = true)
    public List<ThesisResponse> findAll() {
        Long userId = currentUserService.getCurrentUser().id();
        return investmentThesisRepository.findByUser_IdOrderByStock_CodeAsc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ThesisResponse findById(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        return toResponse(findOwnedThesis(id, userId));
    }

    @Transactional
    public ThesisResponse update(Long id, ThesisRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        InvestmentThesis thesis = findOwnedThesis(id, userId);
        if (!thesis.getStock().getId().equals(request.stockId()) && investmentThesisRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("A thesis already exists for this stock");
        }

        applyRequest(thesis, request, userId);
        return toResponse(investmentThesisRepository.save(thesis));
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        investmentThesisRepository.delete(findOwnedThesis(id, userId));
    }

    @Transactional
    public ThesisReviewResponse createReview(Long thesisId, ThesisReviewRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        ThesisReview review = new ThesisReview();
        review.setThesis(findOwnedThesis(thesisId, userId));
        applyReviewRequest(review, request);
        return toReviewResponse(thesisReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ThesisReviewResponse> findReviews(Long thesisId, Pageable pageable) {
        Long userId = currentUserService.getCurrentUser().id();
        findOwnedThesis(thesisId, userId);
        return thesisReviewRepository.findByThesis_IdAndThesis_User_IdOrderByReviewDateDescIdDesc(thesisId, userId, pageable)
            .map(this::toReviewResponse);
    }

    @Transactional
    public ThesisReviewResponse updateReview(Long thesisId, Long reviewId, ThesisReviewRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        findOwnedThesis(thesisId, userId);
        ThesisReview review = findOwnedReview(reviewId, thesisId, userId);
        applyReviewRequest(review, request);
        return toReviewResponse(thesisReviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long thesisId, Long reviewId) {
        Long userId = currentUserService.getCurrentUser().id();
        thesisReviewRepository.delete(findOwnedReview(reviewId, thesisId, userId));
    }

    @Transactional(readOnly = true)
    public ThesisSummaryResponse getSummary() {
        return getSummaryForUser(currentUserService.getCurrentUser().id());
    }

    @Transactional(readOnly = true)
    public ThesisSummaryResponse getSummaryForUser(Long userId) {
        long total = investmentThesisRepository.countByUser_Id(userId);
        long invalidated = thesisReviewRepository.countDistinctByThesis_User_IdAndStillValidFalse(userId);
        long reviewsLast30Days = thesisReviewRepository.countByThesis_User_IdAndReviewDateGreaterThanEqual(userId, LocalDate.now().minusDays(30));
        long active = Math.max(0, total - invalidated);
        return new ThesisSummaryResponse(total, active, invalidated, reviewsLast30Days);
    }

    private void applyRequest(InvestmentThesis thesis, ThesisRequest request, Long userId) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Stock stock = stockService.findEntityById(request.stockId());
        thesis.setUser(user);
        thesis.setStock(stock);
        thesis.setThesis(request.thesis());
        thesis.setRisks(request.risks());
        thesis.setInvalidationCondition(request.invalidationCondition());
        thesis.setHoldingPeriod(request.holdingPeriod());
        thesis.setConfidenceScore(request.confidenceScore());
        thesis.setEmotionTag(request.emotionTag());
    }

    private void applyReviewRequest(ThesisReview review, ThesisReviewRequest request) {
        review.setReviewDate(request.reviewDate());
        review.setStillValid(request.stillValid());
        review.setAction(request.action());
        review.setLesson(request.lesson());
    }

    private InvestmentThesis findOwnedThesis(Long id, Long userId) {
        return investmentThesisRepository.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Thesis not found"));
    }

    private ThesisReview findOwnedReview(Long reviewId, Long thesisId, Long userId) {
        return thesisReviewRepository.findById(reviewId)
            .filter(review -> review.getThesis().getId().equals(thesisId))
            .filter(review -> review.getThesis().getUser().getId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Thesis review not found"));
    }

    private ThesisResponse toResponse(InvestmentThesis thesis) {
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

    private ThesisReviewResponse toReviewResponse(ThesisReview review) {
        return new ThesisReviewResponse(
            review.getId(),
            review.getReviewDate(),
            review.getStillValid(),
            review.getAction(),
            review.getLesson());
    }
}
