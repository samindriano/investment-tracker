package com.sam.finance.sahamlog.reporting.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.dividend.dto.DividendResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendSummaryResponse;
import com.sam.finance.sahamlog.dividend.service.DividendService;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.journal.service.ThesisService;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.portfolio.dto.DashboardSummaryResponse;
import com.sam.finance.sahamlog.portfolio.dto.TransactionFilter;
import com.sam.finance.sahamlog.portfolio.dto.TransactionResponse;
import com.sam.finance.sahamlog.portfolio.service.DashboardService;
import com.sam.finance.sahamlog.portfolio.service.TransactionService;
import com.sam.finance.sahamlog.shared.csv.CsvExportService;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistResponse;
import com.sam.finance.sahamlog.watchlist.service.WatchlistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final List<String> TRANSACTION_HEADERS = List.of(
        "id", "stockCode", "stockName", "type", "transactionDate", "quantityLot", "quantityShare", "price", "fee", "notes");
    private static final List<String> DIVIDEND_HEADERS = List.of(
        "id", "stockCode", "stockName", "cumDate", "paymentDate", "dividendPerShare", "sharesOwned", "taxRate", "grossDividend", "totalTax", "netReceived");
    private static final List<String> WATCHLIST_HEADERS = List.of(
        "id", "stockCode", "stockName", "fairPrice", "cheapPrice", "veryCheapPrice", "expensivePrice", "currentPrice", "valuationZone",
        "premiumDiscountPercentage", "marginOfSafetyPercentage", "notes");

    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final DividendService dividendService;
    private final WatchlistService watchlistService;
    private final ThesisService thesisService;
    private final CsvExportService csvExportService;

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportTransactionsCsv(String stockCode, TransactionType type, LocalDate dateFrom, LocalDate dateTo) {
        List<TransactionResponse> rows = transactionService.findAll(new TransactionFilter(stockCode, type, dateFrom, dateTo));
        return csvExportService.export("transactions", TRANSACTION_HEADERS, rows, row -> List.of(
            row.id().toString(),
            row.stockCode(),
            row.stockName(),
            row.type().name(),
            row.transactionDate().toString(),
            row.quantityLot().toString(),
            row.quantityShare().toString(),
            row.price().toPlainString(),
            row.fee().toPlainString(),
            row.notes() == null ? "" : row.notes()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportDividendsCsv(Integer year) {
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        List<DividendResponse> rows = dividendService.findAll(null, resolvedYear, Pageable.unpaged()).getContent();
        return csvExportService.export("dividends", DIVIDEND_HEADERS, rows, row -> List.of(
            row.id().toString(),
            row.stockCode(),
            row.stockName(),
            row.cumDate() == null ? "" : row.cumDate().toString(),
            row.paymentDate().toString(),
            row.dividendPerShare().toPlainString(),
            row.sharesOwned().toString(),
            row.taxRate().toPlainString(),
            row.grossDividend().toPlainString(),
            row.totalTax().toPlainString(),
            row.netReceived().toPlainString()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportWatchlistCsv() {
        List<WatchlistResponse> rows = watchlistService.findAll();
        return csvExportService.export("watchlist", WATCHLIST_HEADERS, rows, row -> List.of(
            row.id().toString(),
            row.stockCode(),
            row.stockName(),
            row.fairPrice().toPlainString(),
            row.cheapPrice().toPlainString(),
            row.veryCheapPrice().toPlainString(),
            row.expensivePrice().toPlainString(),
            row.currentPrice() == null ? "" : row.currentPrice().toPlainString(),
            row.valuationZone().name(),
            row.premiumDiscountPercentage() == null ? "" : row.premiumDiscountPercentage().toPlainString(),
            row.marginOfSafetyPercentage() == null ? "" : row.marginOfSafetyPercentage().toPlainString(),
            row.notes() == null ? "" : row.notes()));
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getPortfolioReport() {
        return dashboardService.getSummary();
    }

    @Transactional(readOnly = true)
    public DividendSummaryResponse getDividendReport(Integer year) {
        return dividendService.getSummary(year);
    }

    @Transactional(readOnly = true)
    public ThesisSummaryResponse getThesisReport() {
        return thesisService.getSummary();
    }
}
