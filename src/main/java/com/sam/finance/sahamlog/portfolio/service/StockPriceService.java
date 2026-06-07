package com.sam.finance.sahamlog.portfolio.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.domain.StockPriceSnapshot;
import com.sam.finance.sahamlog.portfolio.dto.PriceResponse;
import com.sam.finance.sahamlog.portfolio.dto.PriceUpsertRequest;
import com.sam.finance.sahamlog.portfolio.repository.StockPriceSnapshotRepository;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final StockPriceSnapshotRepository stockPriceSnapshotRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;

    @Transactional
    public PriceResponse upsert(Long stockId, PriceUpsertRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        Stock stock = stockService.findEntityById(stockId);

        StockPriceSnapshot snapshot = stockPriceSnapshotRepository.findByUser_IdAndStock_Id(userId, stockId)
            .orElseGet(StockPriceSnapshot::new);
        snapshot.setUser(currentUserService.getCurrentAppUser());
        snapshot.setStock(stock);
        snapshot.setPrice(request.price());
        snapshot.setPricedAt(request.pricedAt() == null ? OffsetDateTime.now() : request.pricedAt());

        return toResponse(stockPriceSnapshotRepository.save(snapshot));
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> findAll() {
        Long userId = currentUserService.getCurrentUserId();
        return stockPriceSnapshotRepository.findByUser_IdOrderByStock_CodeAsc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PriceResponse findByStockId(Long stockId) {
        Long userId = currentUserService.getCurrentUserId();
        return stockPriceSnapshotRepository.findByUser_IdAndStock_Id(userId, stockId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Price snapshot not found"));
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, StockPriceSnapshot> findSnapshotMapByUserId(Long userId) {
        return stockPriceSnapshotRepository.findByUser_IdOrderByStock_CodeAsc(userId)
            .stream()
            .collect(java.util.stream.Collectors.toMap(snapshot -> snapshot.getStock().getId(), snapshot -> snapshot));
    }

    private PriceResponse toResponse(StockPriceSnapshot snapshot) {
        return new PriceResponse(
            snapshot.getStock().getId(),
            snapshot.getStock().getCode(),
            snapshot.getStock().getName(),
            snapshot.getPrice(),
            snapshot.getPricedAt());
    }
}
