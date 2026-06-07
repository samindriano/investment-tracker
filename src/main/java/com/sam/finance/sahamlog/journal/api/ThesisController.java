package com.sam.finance.sahamlog.journal.api;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

import com.sam.finance.sahamlog.journal.dto.ThesisRequest;
import com.sam.finance.sahamlog.journal.dto.ThesisResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisReviewRequest;
import com.sam.finance.sahamlog.journal.dto.ThesisReviewResponse;
import com.sam.finance.sahamlog.journal.dto.ThesisSummaryResponse;
import com.sam.finance.sahamlog.journal.service.ThesisService;
import com.sam.finance.sahamlog.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theses")
@RequiredArgsConstructor
public class ThesisController {

    private final ThesisService thesisService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThesisResponse create(@Valid @RequestBody ThesisRequest request) {
        return thesisService.create(request);
    }

    @GetMapping
    public List<ThesisResponse> findAll() {
        return thesisService.findAll();
    }

    @GetMapping("/{id}")
    public ThesisResponse findById(@PathVariable Long id) {
        return thesisService.findById(id);
    }

    @PutMapping("/{id}")
    public ThesisResponse update(@PathVariable Long id, @Valid @RequestBody ThesisRequest request) {
        return thesisService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        thesisService.delete(id);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ThesisReviewResponse createReview(@PathVariable Long id, @Valid @RequestBody ThesisReviewRequest request) {
        return thesisService.createReview(id, request);
    }

    @GetMapping("/{id}/reviews")
    public PageResponse<ThesisReviewResponse> findReviews(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        return PageResponse.from(thesisService.findReviews(id, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/reviews/{reviewId}")
    public ThesisReviewResponse updateReview(
        @PathVariable Long id,
        @PathVariable Long reviewId,
        @Valid @RequestBody ThesisReviewRequest request) {

        return thesisService.updateReview(id, reviewId, request);
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        thesisService.deleteReview(id, reviewId);
    }

    @GetMapping("/summary")
    public ThesisSummaryResponse summary() {
        return thesisService.getSummary();
    }
}
