package com.sam.finance.sahamlog.watchlist.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sam.finance.sahamlog.watchlist.dto.WatchlistRequest;
import com.sam.finance.sahamlog.watchlist.dto.WatchlistResponse;
import com.sam.finance.sahamlog.watchlist.service.WatchlistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistResponse create(@Valid @RequestBody WatchlistRequest request) {
        return watchlistService.create(request);
    }

    @GetMapping
    public List<WatchlistResponse> findAll() {
        return watchlistService.findAll();
    }

    @GetMapping("/{id}")
    public WatchlistResponse findById(@PathVariable Long id) {
        return watchlistService.findById(id);
    }

    @PutMapping("/{id}")
    public WatchlistResponse update(@PathVariable Long id, @Valid @RequestBody WatchlistRequest request) {
        return watchlistService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        watchlistService.delete(id);
    }
}
