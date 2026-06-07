package com.sam.finance.sahamlog.reporting.domain;

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
    name = "thesis_status_snapshot",
    uniqueConstraints = @UniqueConstraint(name = "uq_thesis_status_snapshot", columnNames = {"user_id", "snapshot_date"}))
public class ThesisStatusSnapshot extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_theses", nullable = false)
    private Integer totalTheses;

    @Column(name = "active_theses", nullable = false)
    private Integer activeTheses;

    @Column(name = "invalidated_theses", nullable = false)
    private Integer invalidatedTheses;

    @Column(name = "reviews_last_30_days", nullable = false)
    private Integer reviewsLast30Days;
}
