package com.sam.finance.sahamlog.journal.domain;

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
    name = "investment_thesis",
    uniqueConstraints = @UniqueConstraint(name = "uq_investment_thesis_user_stock", columnNames = {"user_id", "stock_id"}))
public class InvestmentThesis extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, columnDefinition = "text")
    private String thesis;

    @Column(columnDefinition = "text")
    private String risks;

    @Column(name = "invalidation_condition", columnDefinition = "text")
    private String invalidationCondition;

    @Column(name = "holding_period", length = 50)
    private String holdingPeriod;

    @Column(name = "confidence_score")
    private Short confidenceScore;

    @Column(name = "emotion_tag", length = 50)
    private String emotionTag;
}
