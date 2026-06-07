package com.sam.finance.sahamlog.reporting.domain;

import java.math.BigDecimal;

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
    name = "dividend_monthly_snapshot",
    uniqueConstraints = @UniqueConstraint(name = "uq_dividend_monthly_snapshot", columnNames = {"user_id", "snapshot_year", "snapshot_month"}))
public class DividendMonthlySnapshot extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "snapshot_year", nullable = false)
    private Integer snapshotYear;

    @Column(name = "snapshot_month", nullable = false)
    private Integer snapshotMonth;

    @Column(name = "total_gross_dividend", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalGrossDividend;

    @Column(name = "total_tax", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTax;

    @Column(name = "total_net_dividend", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalNetDividend;
}
