package com.sam.finance.sahamlog.dividend.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.portfolio.domain.Stock;
import com.sam.finance.sahamlog.shared.domain.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dividend")
public class Dividend extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "cum_date")
    private LocalDate cumDate;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "dividend_per_share", nullable = false, precision = 19, scale = 2)
    private BigDecimal dividendPerShare;

    @Column(name = "shares_owned", nullable = false)
    private Integer sharesOwned;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "net_received", nullable = false, precision = 19, scale = 2)
    private BigDecimal netReceived;
}
