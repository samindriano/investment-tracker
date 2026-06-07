package com.sam.finance.sahamlog.journal.domain;

import java.time.LocalDate;

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
@Table(name = "thesis_review")
public class ThesisReview extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thesis_id", nullable = false)
    private InvestmentThesis thesis;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "still_valid", nullable = false)
    private Boolean stillValid;

    @Column(length = 20)
    private String action;

    @Column(columnDefinition = "text")
    private String lesson;
}
