package com.sam.finance.sahamlog.portfolio.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.portfolio.dto.StockRequest;
import com.sam.finance.sahamlog.portfolio.dto.StockResponse;
import com.sam.finance.sahamlog.portfolio.service.StockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse create(@Valid @RequestBody StockRequest request) {
        return stockService.create(request);
    }

    @GetMapping
    public List<StockResponse> findAll() {
        return stockService.findAll();
    }

    @GetMapping("/{id}")
    public StockResponse findById(@PathVariable Long id) {
        return stockService.findById(id);
    }

    @PutMapping("/{id}")
    public StockResponse update(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return stockService.update(id, request);
    }
}
