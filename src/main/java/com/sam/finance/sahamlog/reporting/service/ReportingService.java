package com.sam.finance.sahamlog.reporting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.dividend.dto.DividendResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendSummaryResponse;
import com.sam.finance.sahamlog.dividend.repository.DividendRepository;
import com.sam.finance.sahamlog.dividend.service.DividendService;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.journal.repository.InvestmentThesisRepository;
import com.sam.finance.sahamlog.journal.service.ThesisService;
import com.sam.finance.sahamlog.portfolio.domain.TransactionType;
import com.sam.finance.sahamlog.portfolio.dto.DashboardSummaryResponse;
import com.sam.finance.sahamlog.portfolio.dto.TransactionFilter;
import com.sam.finance.sahamlog.portfolio.dto.TransactionResponse;
import com.sam.finance.sahamlog.portfolio.service.DashboardService;
import com.sam.finance.sahamlog.portfolio.service.TransactionService;
import com.sam.finance.sahamlog.reporting.domain.DividendMonthlySnapshot;
import com.sam.finance.sahamlog.reporting.domain.PortfolioDailySnapshot;
import com.sam.finance.sahamlog.reporting.domain.ThesisStatusSnapshot;
import com.sam.finance.sahamlog.reporting.repository.DividendMonthlySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.PortfolioDailySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.ThesisStatusSnapshotRepository;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistResponse;
import com.sam.finance.sahamlog.watchlist.service.WatchlistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;
    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final DividendService dividendService;
    private final WatchlistService watchlistService;
    private final ThesisService thesisService;
    private final PortfolioDailySnapshotRepository portfolioDailySnapshotRepository;
    private final DividendMonthlySnapshotRepository dividendMonthlySnapshotRepository;
    private final ThesisStatusSnapshotRepository thesisStatusSnapshotRepository;
    private final DividendRepository dividendRepository;
    private final InvestmentThesisRepository investmentThesisRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportTransactionsCsv(String stockCode, TransactionType type, LocalDate dateFrom, LocalDate dateTo) {
        List<TransactionResponse> rows = transactionService.findAll(new TransactionFilter(stockCode, type, dateFrom, dateTo));
        StringBuilder csv = new StringBuilder("id,stockCode,stockName,type,transactionDate,quantityLot,quantityShare,price,fee,notes\n");
        rows.forEach(row -> csv.append(row.id()).append(',')
            .append(row.stockCode()).append(',')
            .append(escape(row.stockName())).append(',')
            .append(row.type()).append(',')
            .append(row.transactionDate()).append(',')
            .append(row.quantityLot()).append(',')
            .append(row.quantityShare()).append(',')
            .append(row.price()).append(',')
            .append(row.fee()).append(',')
            .append(escape(row.notes())).append('\n'));
        return csvResponse("transactions", csv.toString());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportDividendsCsv(Integer year) {
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        List<DividendResponse> rows = dividendService.findAll(null, resolvedYear, org.springframework.data.domain.Pageable.unpaged()).getContent();
        StringBuilder csv = new StringBuilder("id,stockCode,stockName,cumDate,paymentDate,dividendPerShare,sharesOwned,taxRate,grossDividend,totalTax,netReceived\n");
        rows.forEach(row -> csv.append(row.id()).append(',')
            .append(row.stockCode()).append(',')
            .append(escape(row.stockName())).append(',')
            .append(row.cumDate()).append(',')
            .append(row.paymentDate()).append(',')
            .append(row.dividendPerShare()).append(',')
            .append(row.sharesOwned()).append(',')
            .append(row.taxRate()).append(',')
            .append(row.grossDividend()).append(',')
            .append(row.totalTax()).append(',')
            .append(row.netReceived()).append('\n'));
        return csvResponse("dividends", csv.toString());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> exportWatchlistCsv() {
        List<WatchlistResponse> rows = watchlistService.findAll();
        StringBuilder csv = new StringBuilder("id,stockCode,stockName,fairPrice,cheapPrice,veryCheapPrice,expensivePrice,currentPrice,valuationZone,premiumDiscountPercentage,marginOfSafetyPercentage,notes\n");
        rows.forEach(row -> csv.append(row.id()).append(',')
            .append(row.stockCode()).append(',')
            .append(escape(row.stockName())).append(',')
            .append(row.fairPrice()).append(',')
            .append(row.cheapPrice()).append(',')
            .append(row.veryCheapPrice()).append(',')
            .append(row.expensivePrice()).append(',')
            .append(row.currentPrice()).append(',')
            .append(row.valuationZone()).append(',')
            .append(row.premiumDiscountPercentage()).append(',')
            .append(row.marginOfSafetyPercentage()).append(',')
            .append(escape(row.notes())).append('\n'));
        return csvResponse("watchlist", csv.toString());
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

    @Transactional
    @Scheduled(cron = "0 5 1 * * *")
    public void captureDailySnapshots() {
        LocalDate today = LocalDate.now();
        for (AppUser user : appUserRepository.findAll()) {
            DashboardSummaryResponse dashboard = dashboardService.getSummaryForUser(user.getId());

            PortfolioDailySnapshot portfolio = portfolioDailySnapshotRepository.findByUser_IdAndSnapshotDate(user.getId(), today)
                .orElseGet(PortfolioDailySnapshot::new);
            portfolio.setUser(user);
            portfolio.setSnapshotDate(today);
            portfolio.setTotalModal(dashboard.totalModal());
            portfolio.setTotalMarketValue(dashboard.totalMarketValue());
            portfolio.setTotalUnrealizedGainLoss(dashboard.totalUnrealizedGainLoss());
            portfolioDailySnapshotRepository.save(portfolio);

            ThesisSummaryResponse thesisSummary = thesisService.getSummaryForUser(user.getId());
            ThesisStatusSnapshot thesisSnapshot = thesisStatusSnapshotRepository.findByUser_IdAndSnapshotDate(user.getId(), today)
                .orElseGet(ThesisStatusSnapshot::new);
            thesisSnapshot.setUser(user);
            thesisSnapshot.setSnapshotDate(today);
            thesisSnapshot.setTotalTheses((int) thesisSummary.totalTheses());
            thesisSnapshot.setActiveTheses((int) thesisSummary.activeTheses());
            thesisSnapshot.setInvalidatedTheses((int) thesisSummary.invalidatedTheses());
            thesisSnapshot.setReviewsLast30Days((int) thesisSummary.reviewsLast30Days());
            thesisStatusSnapshotRepository.save(thesisSnapshot);
        }
    }

    @Transactional
    @Scheduled(cron = "0 10 1 1 * *")
    public void captureMonthlyDividendSnapshots() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        int year = previousMonth.getYear();
        int month = previousMonth.getMonthValue();
        for (AppUser user : appUserRepository.findAll()) {
            List<com.sam.finance.sahamlog.dividend.domain.Dividend> dividends = dividendRepository.findByUser_IdAndPaymentDateBetweenOrderByPaymentDateAsc(
                user.getId(),
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, previousMonth.lengthOfMonth()));
            BigDecimal gross = dividends.stream()
                .map(dividend -> dividend.getDividendPerShare().multiply(BigDecimal.valueOf(dividend.getSharesOwned())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = dividends.stream()
                .map(com.sam.finance.sahamlog.dividend.domain.Dividend::getNetReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = gross.subtract(net);

            DividendMonthlySnapshot snapshot = dividendMonthlySnapshotRepository.findByUser_IdAndSnapshotYearAndSnapshotMonth(user.getId(), year, month)
                .orElseGet(DividendMonthlySnapshot::new);
            snapshot.setUser(user);
            snapshot.setSnapshotYear(year);
            snapshot.setSnapshotMonth(month);
            snapshot.setTotalGrossDividend(gross);
            snapshot.setTotalTax(tax);
            snapshot.setTotalNetDividend(net);
            dividendMonthlySnapshotRepository.save(snapshot);
        }
    }

    private ResponseEntity<String> csvResponse(String prefix, String csv) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s-%s.csv".formatted(prefix, LocalDateTime.now().format(FILE_TS)))
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
