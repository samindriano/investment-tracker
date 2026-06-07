package com.sam.finance.sahamlog.watchlist.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.StockPriceSnapshot;
import com.sam.finance.sahamlog.portfolio.service.StockPriceService;
import com.sam.finance.sahamlog.portfolio.service.StockService;
import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;
import com.sam.finance.sahamlog.watchlist.domain.WatchlistItem;
import com.sam.finance.sahamlog.watchlist.dto.ValuationZone;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistRequest;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistResponse;
import com.sam.finance.sahamlog.watchlist.repository.WatchlistItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final WatchlistItemRepository watchlistItemRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;
    private final StockPriceService stockPriceService;

    @Transactional
    public WatchlistResponse create(WatchlistRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        if (watchlistItemRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("Watchlist item already exists for this stock");
        }

        validateBands(request);
        WatchlistItem item = new WatchlistItem();
        applyRequest(item, request, userId);
        return toResponse(watchlistItemRepository.save(item), userId);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> findAll() {
        Long userId = currentUserService.getCurrentUser().id();
        return watchlistItemRepository.findByUser_IdOrderByStock_CodeAsc(userId)
            .stream()
            .map(item -> toResponse(item, userId))
            .toList();
    }

    @Transactional(readOnly = true)
    public WatchlistResponse findById(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        return toResponse(findOwnedItem(id, userId), userId);
    }

    @Transactional
    public WatchlistResponse update(Long id, WatchlistRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        validateBands(request);
        WatchlistItem item = findOwnedItem(id, userId);
        if (!item.getStock().getId().equals(request.stockId()) && watchlistItemRepository.existsByUser_IdAndStock_Id(userId, request.stockId())) {
            throw new ConflictException("Watchlist item already exists for this stock");
        }

        applyRequest(item, request, userId);
        return toResponse(watchlistItemRepository.save(item), userId);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        watchlistItemRepository.delete(findOwnedItem(id, userId));
    }

    private void applyRequest(WatchlistItem item, WatchlistRequest request, Long userId) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Stock stock = stockService.findEntityById(request.stockId());
        item.setUser(user);
        item.setStock(stock);
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
        ValuationZone zone = resolveZone(item, currentPrice);
        BigDecimal premiumDiscount = currentPrice == null || item.getFairPrice().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : currentPrice.subtract(item.getFairPrice()).divide(item.getFairPrice(), 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
        BigDecimal marginOfSafety = currentPrice == null || item.getFairPrice().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : item.getFairPrice().subtract(currentPrice).divide(item.getFairPrice(), 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);

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
            zone,
            premiumDiscount,
            marginOfSafety);
    }

    private ValuationZone resolveZone(WatchlistItem item, BigDecimal currentPrice) {
        if (currentPrice == null) {
            return ValuationZone.NO_PRICE;
        }
        if (currentPrice.compareTo(item.getVeryCheapPrice()) <= 0) {
            return ValuationZone.VERY_CHEAP;
        }
        if (currentPrice.compareTo(item.getCheapPrice()) <= 0) {
            return ValuationZone.CHEAP;
        }
        if (currentPrice.compareTo(item.getFairPrice()) <= 0) {
            return ValuationZone.FAIR;
        }
        if (currentPrice.compareTo(item.getExpensivePrice()) <= 0) {
            return ValuationZone.EXPENSIVE;
        }
        return ValuationZone.OVERPRICED;
    }
}
