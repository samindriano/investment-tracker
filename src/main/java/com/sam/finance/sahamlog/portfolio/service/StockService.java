package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.dto.StockRequest;
import com.sam.finance.sahamlog.portfolio.dto.StockResponse;
import com.sam.finance.sahamlog.portfolio.repository.StockRepository;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public StockResponse create(StockRequest request) {
        String normalizedCode = normalizeCode(request.code());
        if (stockRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ConflictException("Stock code is already registered");
        }

        Stock stock = new Stock();
        stock.setCode(normalizedCode);
        stock.setName(normalizeText(request.name()));
        stock.setSector(normalizeNullableText(request.sector()));

        return toResponse(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAll() {
        return stockRepository.findAll()
            .stream()
            .sorted((left, right) -> left.getCode().compareToIgnoreCase(right.getCode()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public StockResponse update(Long id, StockRequest request) {
        Stock stock = findEntityById(id);
        String normalizedCode = normalizeCode(request.code());
        if (!stock.getCode().equalsIgnoreCase(normalizedCode) && stockRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ConflictException("Stock code is already registered");
        }

        stock.setCode(normalizedCode);
        stock.setName(normalizeText(request.name()));
        stock.setSector(normalizeNullableText(request.sector()));

        return toResponse(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public Stock findEntityById(Long id) {
        return stockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(stock.getId(), stock.getCode(), stock.getName(), stock.getSector());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private String normalizeText(String value) {
        return value.trim();
    }

    private String normalizeNullableText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
