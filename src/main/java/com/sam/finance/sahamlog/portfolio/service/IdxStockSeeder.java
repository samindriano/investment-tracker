package com.sam.finance.sahamlog.portfolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@RequiredArgsConstructor
public class IdxStockSeeder implements CommandLineRunner {

    private final StockService stockService;

    @Value("${app.stock-seed.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (enabled) {
            stockService.seedDefaults();
        }
    }
}
