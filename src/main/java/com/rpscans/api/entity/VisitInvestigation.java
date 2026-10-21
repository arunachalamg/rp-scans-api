package com.rpscans.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "visit_investigations")
public class VisitInvestigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(optional = false)
    public Visit visit;
    @ManyToOne(optional = false)
    public Investigation investigation;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;
    @Column(nullable = false)
    public boolean concession = false;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal discountAmount = BigDecimal.ZERO;
    public String discountReason;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal referralAmount = BigDecimal.ZERO;
    @Column(name="referral_code", length=100)
    public String referralCode;
    @Column(nullable = false)
    public boolean cancelled = false;
    @Column(name = "cancellation_reason", length = 1000)
    public String cancellationReason;
    @Column(name = "cancelled_at")
    public java.time.OffsetDateTime cancelledAt;
}
