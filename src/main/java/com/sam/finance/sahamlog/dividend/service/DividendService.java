package com.sam.finance.sahamlog.dividend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.dividend.domain.Dividend;
import com.sam.finance.sahamlog.dividend.dto.DividendCalendarItemResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendCalendarResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendMonthSummaryResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendRequest;
import com.sam.finance.sahamlog.dividend.dto.DividendResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendStockSummaryResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendSummaryResponse;
import com.sam.finance.sahamlog.dividend.repository.DividendRepository;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.portfolio.service.PortfolioService;
import com.sam.finance.sahamlog.portfolio.service.PositionSnapshot;
import com.sam.finance.sahamlog.portfolio.service.StockService;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DividendService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final DividendRepository dividendRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;
    private final PortfolioService portfolioService;

    @Transactional
    public DividendResponse create(DividendRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        Dividend dividend = new Dividend();
        applyRequest(dividend, request, userId);
        return toResponse(dividendRepository.save(dividend));
    }

    @Transactional(readOnly = true)
    public Page<DividendResponse> findAll(String stockCode, Integer year, Pageable pageable) {
        Long userId = currentUserService.getCurrentUser().id();
        LocalDate from = year == null ? LocalDate.of(1900, 1, 1) : LocalDate.of(year, 1, 1);
        LocalDate to = year == null ? LocalDate.of(2999, 12, 31) : LocalDate.of(year, 12, 31);
        Page<Dividend> page = stockCode == null || stockCode.isBlank()
            ? dividendRepository.findByUser_IdAndPaymentDateBetween(userId, from, to, pageable)
            : dividendRepository.findByUser_IdAndStock_CodeContainingIgnoreCaseAndPaymentDateBetween(
                userId,
                stockCode.trim(),
                from,
                to,
                pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DividendResponse findById(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        return toResponse(findOwnedDividend(id, userId));
    }

    @Transactional
    public DividendResponse update(Long id, DividendRequest request) {
        Long userId = currentUserService.getCurrentUser().id();
        Dividend dividend = findOwnedDividend(id, userId);
        applyRequest(dividend, request, userId);
        return toResponse(dividendRepository.save(dividend));
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().id();
        dividendRepository.delete(findOwnedDividend(id, userId));
    }

    @Transactional(readOnly = true)
    public DividendSummaryResponse getSummary(Integer year) {
        Long userId = currentUserService.getCurrentUser().id();
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        List<Dividend> dividends = findDividendsForYear(userId, resolvedYear);
        Map<Long, PositionSnapshot> holdingsByStockId = portfolioService.calculatePositions(userId)
            .stream()
            .collect(Collectors.toMap(position -> position.stock().getId(), position -> position));

        BigDecimal totalGross = dividends.stream().map(this::grossDividend).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalNet = dividends.stream().map(Dividend::getNetReceived).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTax = totalGross.subtract(totalNet).setScale(2, RoundingMode.HALF_UP);

        List<DividendStockSummaryResponse> byStock = dividends.stream()
            .collect(Collectors.groupingBy(dividend -> dividend.getStock().getId()))
            .values()
            .stream()
            .map(items -> {
                Dividend first = items.getFirst();
                BigDecimal stockGross = items.stream().map(this::grossDividend).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal stockNet = items.stream().map(Dividend::getNetReceived).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal stockTax = stockGross.subtract(stockNet).setScale(2, RoundingMode.HALF_UP);
                PositionSnapshot position = holdingsByStockId.get(first.getStock().getId());
                BigDecimal yieldOnCost = position == null || position.totalCostBasis().compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : stockNet.divide(position.totalCostBasis(), 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);

                return new DividendStockSummaryResponse(
                    first.getStock().getId(),
                    first.getStock().getCode(),
                    first.getStock().getName(),
                    stockGross,
                    stockTax,
                    stockNet,
                    yieldOnCost);
            })
            .sorted((left, right) -> left.stockCode().compareToIgnoreCase(right.stockCode()))
            .toList();

        List<DividendMonthSummaryResponse> byMonth = dividends.stream()
            .collect(Collectors.groupingBy(dividend -> dividend.getPaymentDate().getMonthValue()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                BigDecimal stockGross = entry.getValue().stream().map(this::grossDividend).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal stockNet = entry.getValue().stream().map(Dividend::getNetReceived).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal stockTax = stockGross.subtract(stockNet).setScale(2, RoundingMode.HALF_UP);
                return new DividendMonthSummaryResponse(entry.getKey(), stockGross, stockTax, stockNet);
            })
            .toList();

        return new DividendSummaryResponse(resolvedYear, totalGross, totalTax, totalNet, byStock, byMonth);
    }

    @Transactional(readOnly = true)
    public DividendCalendarResponse getCalendar(Integer year, Integer month) {
        Long userId = currentUserService.getCurrentUser().id();
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        int resolvedMonth = month == null ? LocalDate.now().getMonthValue() : month;
        LocalDate from = LocalDate.of(resolvedYear, resolvedMonth, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<DividendCalendarItemResponse> items = dividendRepository.findByUser_IdAndPaymentDateBetweenOrderByPaymentDateAsc(userId, from, to)
            .stream()
            .map(dividend -> new DividendCalendarItemResponse(
                dividend.getId(),
                dividend.getStock().getCode(),
                dividend.getStock().getName(),
                dividend.getPaymentDate(),
                dividend.getNetReceived()))
            .toList();

        return new DividendCalendarResponse(resolvedYear, resolvedMonth, items);
    }

    @Transactional(readOnly = true)
    public List<Dividend> findDividendsForYear(Long userId, int year) {
        return dividendRepository.findByUser_IdAndPaymentDateBetweenOrderByPaymentDateAsc(
            userId,
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31));
    }

    private void applyRequest(Dividend dividend, DividendRequest request, Long userId) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Stock stock = stockService.findEntityById(request.stockId());
        dividend.setUser(user);
        dividend.setStock(stock);
        dividend.setCumDate(request.cumDate());
        dividend.setPaymentDate(request.paymentDate());
        dividend.setDividendPerShare(request.dividendPerShare().setScale(2, RoundingMode.HALF_UP));
        dividend.setSharesOwned(request.sharesOwned());
        dividend.setTaxRate(request.taxRate().setScale(2, RoundingMode.HALF_UP));
        dividend.setNetReceived(calculateNetReceived(request.dividendPerShare(), request.sharesOwned(), request.taxRate()));
    }

    private Dividend findOwnedDividend(Long id, Long userId) {
        return dividendRepository.findById(id)
            .filter(dividend -> dividend.getUser().getId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Dividend not found"));
    }

    private BigDecimal grossDividend(Dividend dividend) {
        return dividend.getDividendPerShare()
            .multiply(BigDecimal.valueOf(dividend.getSharesOwned()))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetReceived(BigDecimal dividendPerShare, Integer sharesOwned, BigDecimal taxRate) {
        BigDecimal gross = dividendPerShare.multiply(BigDecimal.valueOf(sharesOwned));
        BigDecimal taxMultiplier = BigDecimal.ONE.subtract(taxRate.divide(HUNDRED, 4, RoundingMode.HALF_UP));
        return gross.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private DividendResponse toResponse(Dividend dividend) {
        BigDecimal gross = grossDividend(dividend);
        return new DividendResponse(
            dividend.getId(),
            dividend.getStock().getId(),
            dividend.getStock().getCode(),
            dividend.getStock().getName(),
            dividend.getCumDate(),
            dividend.getPaymentDate(),
            dividend.getDividendPerShare(),
            dividend.getSharesOwned(),
            dividend.getTaxRate(),
            gross,
            gross.subtract(dividend.getNetReceived()).setScale(2, RoundingMode.HALF_UP),
            dividend.getNetReceived());
    }
}
