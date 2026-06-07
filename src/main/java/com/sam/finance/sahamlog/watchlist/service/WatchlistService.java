package com.sam.finance.sahamlog.watchlist.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.StockPriceSnapshot;
import com.sam.finance.sahamlog.portfolio.service.StockPriceService;
import com.sam.finance.sahamlog.portfolio.service.StockService;
import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;
import com.sam.finance.sahamlog.watchlist.domain.WatchlistItem;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistRequest;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistResponse;
import com.sam.finance.sahamlog.watchlist.repository.WatchlistItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;
    private final StockPriceService stockPriceService;
    private final WatchlistValuationPolicy watchlistValuationPolicy;

    @Transactional
    public WatchlistResponse create(WatchlistRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        if (watchlistItemRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("Watchlist item already exists for this stock");
        }

        validateBands(request);
        WatchlistItem item = new WatchlistItem();
        applyRequest(item, request);
        return toResponse(watchlistItemRepository.save(item), userId);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> findAll() {
        Long userId = currentUserService.getCurrentUserId();
        return watchlistItemRepository.findByUser_IdOrderByStock_CodeAsc(userId)
            .stream()
            .map(item -> toResponse(item, userId))
            .toList();
    }

    @Transactional(readOnly = true)
    public WatchlistResponse findById(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return toResponse(findOwnedItem(id, userId), userId);
    }

    @Transactional
    public WatchlistResponse update(Long id, WatchlistRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        validateBands(request);
        WatchlistItem item = findOwnedItem(id, userId);
        if (!item.getStock().getId().equals(request.stockId()) && watchlistItemRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("Watchlist item already exists for this stock");
        }

        applyRequest(item, request);
        return toResponse(watchlistItemRepository.save(item), userId);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        watchlistItemRepository.delete(findOwnedItem(id, userId));
    }

    private void applyRequest(WatchlistItem item, WatchlistRequest request) {
        item.setUser(currentUserService.getCurrentAppUser());
        item.setStock(stockService.findEntityById(request.stockId()));
        item.setFairPrice(request.fairPrice().setScale(2, RoundingMode.HALF_UP));
        item.setCheapPrice(request.cheapPrice().setScale(2, RoundingMode.HALF_UP));
        item.setVeryCheapPrice(request.veryCheapPrice().setScale(2, RoundingMode.HALF_UP));
        item.setExpensivePrice(request.expensivePrice().setScale(2, RoundingMode.HALF_UP));
        item.setNotes(request.notes());
    }

    private void validateBands(WatchlistRequest request) {
        if (request.veryCheapPrice().compareTo(request.cheapPrice()) > 0
            || request.cheapPrice().compareTo(request.fairPrice()) > 0
            || request.fairPrice().compareTo(request.expensivePrice()) > 0) {
            throw new BusinessRuleViolationException("Watchlist price bands must be ordered very cheap <= cheap <= fair <= expensive");
        }
    }

    private WatchlistItem findOwnedItem(Long id, Long userId) {
        return watchlistItemRepository.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Watchlist item not found"));
    }

    private WatchlistResponse toResponse(WatchlistItem item, Long userId) {
        StockPriceSnapshot snapshot = stockPriceService.findSnapshotMapByUserId(userId).get(item.getStock().getId());
        BigDecimal currentPrice = snapshot == null ? null : snapshot.getPrice();
        WatchlistValuation valuation = watchlistValuationPolicy.evaluate(item, currentPrice);

        return new WatchlistResponse(
            item.getId(),
            item.getStock().getId(),
            item.getStock().getCode(),
            item.getStock().getName(),
            item.getFairPrice(),
            item.getCheapPrice(),
            item.getVeryCheapPrice(),
            item.getExpensivePrice(),
            item.getNotes(),
            currentPrice,
            valuation.zone(),
            valuation.premiumDiscountPercentage(),
            valuation.marginOfSafetyPercentage());
    }
}
