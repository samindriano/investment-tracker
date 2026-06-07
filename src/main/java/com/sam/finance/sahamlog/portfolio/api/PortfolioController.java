package com.sam.finance.sahamlog.portfolio.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.portfolio.dto.HoldingResponse;
import com.sam.finance.sahamlog.portfolio.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/holdings")
    public List<HoldingResponse> getHoldings() {
        return portfolioService.getHoldings();
    }
}
