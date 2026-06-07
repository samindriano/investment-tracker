package com.sam.finance.sahamlog.watchlist.domain;

import java.math.BigDecimal;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.shared.domain.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "watchlist_item",
    uniqueConstraints = @UniqueConstraint(name = "uq_watchlist_item_user_stock", columnNames = {"user_id", "stock_id"}))
public class WatchlistItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "fair_price", precision = 19, scale = 2)
    private BigDecimal fairPrice;

    @Column(name = "cheap_price", precision = 19, scale = 2)
    private BigDecimal cheapPrice;

    @Column(name = "very_cheap_price", precision = 19, scale = 2)
    private BigDecimal veryCheapPrice;

    @Column(name = "expensive_price", precision = 19, scale = 2)
    private BigDecimal expensivePrice;

    @Column(columnDefinition = "text")
    private String notes;
}
