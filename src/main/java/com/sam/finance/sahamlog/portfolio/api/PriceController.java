package com.sam.finance.sahamlog.portfolio.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.portfolio.dto.PriceResponse;
import com.sam.finance.sahamlog.portfolio.dto.PriceUpsertRequest;
import com.sam.finance.sahamlog.portfolio.service.StockPriceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

    private final StockPriceService stockPriceService;

    @PutMapping("/{stockId}")
    public PriceResponse upsert(@PathVariable Long stockId, @Valid @RequestBody PriceUpsertRequest request) {
        return stockPriceService.upsert(stockId, request);
    }

    @GetMapping
    public List<PriceResponse> findAll() {
        return stockPriceService.findAll();
    }

    @GetMapping("/{stockId}")
    public PriceResponse findByStockId(@PathVariable Long stockId) {
        return stockPriceService.findByStockId(stockId);
    }
}
