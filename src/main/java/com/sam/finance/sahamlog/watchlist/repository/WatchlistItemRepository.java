package com.sam.finance.sahamlog.watchlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sam.finance.sahamlog.watchlist.domain.WatchlistItem;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUser_IdOrderByStock_CodeAsc(Long userId);

    Optional<WatchlistItem> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndStock_Id(Long userId, Long stockId);

    boolean existsByStock_Id(Long stockId);
}
