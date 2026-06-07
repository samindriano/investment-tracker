package com.sam.finance.sahamlog.reporting.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    name = "portfolio_daily_snapshot",
    uniqueConstraints = @UniqueConstraint(name = "uq_portfolio_daily_snapshot", columnNames = {"user_id", "snapshot_date"}))
public class PortfolioDailySnapshot extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_modal", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalModal;

    @Column(name = "total_market_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMarketValue;

    @Column(name = "total_unrealized_gain_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalUnrealizedGainLoss;
}
