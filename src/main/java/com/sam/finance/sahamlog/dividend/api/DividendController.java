package com.sam.finance.sahamlog.dividend.api;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.dividend.dto.DividendCalendarResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendRequest;
import com.sam.finance.sahamlog.dividend.dto.DividendResponse;
import com.sam.finance.sahamlog.dividend.dto.DividendSummaryResponse;
import com.sam.finance.sahamlog.dividend.service.DividendService;
import com.sam.finance.sahamlog.shared.dto.PageableFactory;
import com.sam.finance.sahamlog.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/dividends")
@RequiredArgsConstructor
public class DividendController {

    private static final List<Sort.Order> DEFAULT_SORT = List.of(
        Sort.Order.desc("paymentDate"),
        Sort.Order.desc("id"));

    private final DividendService dividendService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DividendResponse create(@Valid @RequestBody DividendRequest request) {
        return dividendService.create(request);
    }

    @GetMapping
    public PageResponse<DividendResponse> findAll(
        @RequestParam(required = false) String stockCode,
        @RequestParam(required = false) Integer year,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) List<String> sort) {

        return PageResponse.from(dividendService.findAll(stockCode, year, PageableFactory.create(page, size, sort, DEFAULT_SORT)));
    }

    @GetMapping("/{id}")
    public DividendResponse findById(@PathVariable Long id) {
        return dividendService.findById(id);
    }

    @PutMapping("/{id}")
    public DividendResponse update(@PathVariable Long id, @Valid @RequestBody DividendRequest request) {
        return dividendService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dividendService.delete(id);
    }

    @GetMapping("/summary")
    public DividendSummaryResponse summary(@RequestParam(required = false) Integer year) {
        return dividendService.getSummary(year);
    }

    @GetMapping("/calendar")
    public DividendCalendarResponse calendar(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month) {

        return dividendService.getCalendar(year, month);
    }
}
