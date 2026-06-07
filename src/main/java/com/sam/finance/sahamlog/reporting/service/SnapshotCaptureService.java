package com.sam.finance.sahamlog.reporting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.dividend.domain.Dividend;
import com.sam.finance.sahamlog.dividend.repository.DividendRepository;
import com.sam.finance.sahamlog.dividend.service.DividendCalculator;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.journal.service.ThesisService;
import com.sam.finance.sahamlog.portfolio.dto.DashboardSummaryResponse;
import com.sam.finance.sahamlog.portfolio.service.DashboardService;
import com.sam.finance.sahamlog.reporting.domain.DividendMonthlySnapshot;
import com.sam.finance.sahamlog.reporting.domain.PortfolioDailySnapshot;
import com.sam.finance.sahamlog.reporting.domain.ThesisStatusSnapshot;
import com.sam.finance.sahamlog.reporting.repository.DividendMonthlySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.PortfolioDailySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.ThesisStatusSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotCaptureService {

    private final AppUserRepository appUserRepository;
    private final DashboardService dashboardService;
    private final ThesisService thesisService;
    private final DividendRepository dividendRepository;
    private final DividendCalculator dividendCalculator;
    private final PortfolioDailySnapshotRepository portfolioDailySnapshotRepository;
    private final ThesisStatusSnapshotRepository thesisStatusSnapshotRepository;
    private final DividendMonthlySnapshotRepository dividendMonthlySnapshotRepository;

    @Transactional
    @Scheduled(cron = "0 5 1 * * *")
    public void captureDailySnapshots() {
        LocalDate today = LocalDate.now();
        for (AppUser user : appUserRepository.findAll()) {
            capturePortfolioSnapshot(user, today);
            captureThesisSnapshot(user, today);
        }
    }

    @Transactional
    @Scheduled(cron = "0 10 1 1 * *")
    public void captureMonthlyDividendSnapshots() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        int year = previousMonth.getYear();
        int month = previousMonth.getMonthValue();

        for (AppUser user : appUserRepository.findAll()) {
            captureDividendSnapshot(user, year, month);
        }
    }

    private void capturePortfolioSnapshot(AppUser user, LocalDate snapshotDate) {
        DashboardSummaryResponse dashboard = dashboardService.getSummaryForUser(user.getId());
        PortfolioDailySnapshot snapshot = portfolioDailySnapshotRepository.findByUser_IdAndSnapshotDate(user.getId(), snapshotDate)
            .orElseGet(PortfolioDailySnapshot::new);

        snapshot.setUser(user);
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setTotalModal(dashboard.totalModal());
        snapshot.setTotalMarketValue(dashboard.totalMarketValue());
        snapshot.setTotalUnrealizedGainLoss(dashboard.totalUnrealizedGainLoss());
        portfolioDailySnapshotRepository.save(snapshot);
    }

    private void captureThesisSnapshot(AppUser user, LocalDate snapshotDate) {
        ThesisSummaryResponse summary = thesisService.getSummaryForUser(user.getId());
        ThesisStatusSnapshot snapshot = thesisStatusSnapshotRepository.findByUser_IdAndSnapshotDate(user.getId(), snapshotDate)
            .orElseGet(ThesisStatusSnapshot::new);

        snapshot.setUser(user);
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setTotalTheses((int) summary.totalTheses());
        snapshot.setActiveTheses((int) summary.activeTheses());
        snapshot.setInvalidatedTheses((int) summary.invalidatedTheses());
        snapshot.setReviewsLast30Days((int) summary.reviewsLast30Days());
        thesisStatusSnapshotRepository.save(snapshot);
    }

    private void captureDividendSnapshot(AppUser user, int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        List<Dividend> dividends = dividendRepository.findByUser_IdAndPaymentDateBetweenOrderByPaymentDateAsc(
            user.getId(),
            firstDayOfMonth,
            firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth()));
        DividendSummary summary = summarizeDividends(dividends);

        DividendMonthlySnapshot snapshot = dividendMonthlySnapshotRepository.findByUser_IdAndSnapshotYearAndSnapshotMonth(user.getId(), year, month)
            .orElseGet(DividendMonthlySnapshot::new);
        snapshot.setUser(user);
        snapshot.setSnapshotYear(year);
        snapshot.setSnapshotMonth(month);
        snapshot.setTotalGrossDividend(summary.totalGrossDividend());
        snapshot.setTotalTax(summary.totalTax());
        snapshot.setTotalNetDividend(summary.totalNetDividend());
        dividendMonthlySnapshotRepository.save(snapshot);
    }

    private DividendSummary summarizeDividends(List<Dividend> dividends) {
        BigDecimal gross = dividends.stream()
            .map(dividend -> dividendCalculator.grossDividend(dividend.getDividendPerShare(), dividend.getSharesOwned()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = dividends.stream()
            .map(Dividend::getNetReceived)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = gross.subtract(net);
        return new DividendSummary(gross, tax, net);
    }

    private record DividendSummary(
        BigDecimal totalGrossDividend,
        BigDecimal totalTax,
        BigDecimal totalNetDividend) {
    }
}
