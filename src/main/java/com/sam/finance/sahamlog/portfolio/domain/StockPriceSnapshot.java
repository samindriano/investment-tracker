package com.sam.finance.sahamlog.portfolio.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.sam.finance.sahamlog.auth.domain.AppUser;
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
    name = "stock_price_snapshot",
    uniqueConstraints = @UniqueConstraint(name = "uq_stock_price_snapshot_user_stock", columnNames = {"user_id", "stock_id"}))
public class StockPriceSnapshot extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "priced_at", nullable = false)
    private OffsetDateTime pricedAt;
}
