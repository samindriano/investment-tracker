package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.dividend.repository.DividendRepository;
import com.sam.finance.sahamlog.journal.repository.InvestmentThesisRepository;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.dto.StockRequest;
import com.sam.finance.sahamlog.portfolio.dto.StockResponse;
import com.sam.finance.sahamlog.portfolio.dto.StockSeedSummary;
import com.sam.finance.sahamlog.portfolio.repository.StockPriceSnapshotRepository;
import com.sam.finance.sahamlog.portfolio.repository.StockRepository;
import com.sam.finance.sahamlog.portfolio.repository.TransactionEntryRepository;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;
import com.sam.finance.sahamlog.watchlist.repository.WatchlistItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final StockPriceSnapshotRepository stockPriceSnapshotRepository;
    private final DividendRepository dividendRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final InvestmentThesisRepository investmentThesisRepository;

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

    @Transactional
    public void delete(Long id) {
        Stock stock = findEntityById(id);
        if (transactionEntryRepository.existsByStock_Id(id)
            || stockPriceSnapshotRepository.existsByStock_Id(id)
            || dividendRepository.existsByStock_Id(id)
            || watchlistItemRepository.existsByStock_Id(id)
            || investmentThesisRepository.existsByStock_Id(id)) {
            throw new ConflictException("Stock cannot be deleted because it is already referenced by portfolio data");
        }

        stockRepository.delete(stock);
    }

    @Transactional
    public StockSeedSummary seedDefaults() {
        List<Stock> defaults = List.of(
            stock("BBCA", "Bank Central Asia", "Banking"),
            stock("BBRI", "Bank Rakyat Indonesia", "Banking"),
            stock("BMRI", "Bank Mandiri", "Banking"),
            stock("TLKM", "Telkom Indonesia", "Telecommunications"),
            stock("ASII", "Astra International", "Conglomerate"),
            stock("ICBP", "Indofood CBP", "Consumer Goods"),
            stock("UNVR", "Unilever Indonesia", "Consumer Goods"),
            stock("MDKA", "Merdeka Copper Gold", "Mining"));

        int insertedCount = 0;
        for (Stock defaultStock : defaults) {
            if (!stockRepository.existsByCodeIgnoreCase(defaultStock.getCode())) {
                stockRepository.save(defaultStock);
                insertedCount++;
            }
        }

        return new StockSeedSummary(insertedCount);
    }

    @Transactional(readOnly = true)
    public Stock findEntityById(Long id) {
        return stockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(stock.getId(), stock.getCode(), stock.getName(), stock.getSector());
    }

    private Stock stock(String code, String name, String sector) {
        Stock stock = new Stock();
        stock.setCode(code);
        stock.setName(name);
        stock.setSector(sector);
        return stock;
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
