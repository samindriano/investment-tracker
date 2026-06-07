package com.sam.finance.sahamlog.reporting.api;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.dividend.dto.DividendSummaryResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.portfolio.dto.DashboardSummaryResponse;
import com.sam.finance.sahamlog.reporting.service.ReportingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/transactions/export.csv")
    public ResponseEntity<String> exportTransactions(
        @RequestParam(required = false) String stockCode,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        return reportingService.exportTransactionsCsv(stockCode, type, dateFrom, dateTo);
    }

    @GetMapping("/dividends/export.csv")
    public ResponseEntity<String> exportDividends(@RequestParam(required = false) Integer year) {
        return reportingService.exportDividendsCsv(year);
    }

    @GetMapping("/watchlist/export.csv")
    public ResponseEntity<String> exportWatchlist() {
        return reportingService.exportWatchlistCsv();
    }

    @GetMapping("/reports/portfolio")
    public DashboardSummaryResponse portfolioReport() {
        return reportingService.getPortfolioReport();
    }

    @GetMapping("/reports/dividends")
    public DividendSummaryResponse dividendReport(@RequestParam(required = false) Integer year) {
        return reportingService.getDividendReport(year);
    }

    @GetMapping("/reports/theses")
    public ThesisSummaryResponse thesisReport() {
        return reportingService.getThesisReport();
    }
}
