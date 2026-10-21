package com.rpscans.api.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "investigation_status_audit")
public class InvestigationStatusAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name="visit_investigation_id", nullable=false)
    public Long visitInvestigationId;
    @Column(name="visit_id", nullable=false)
    public Long visitId;
    @Column(nullable=false, length=20)
    public String action;
    @Column(nullable=false, length=1000)
    public String reason;
    @Column(name="changed_at",nullable=false)
    public OffsetDateTime changedAt;
    public InvestigationStatusAudit() {}
    public InvestigationStatusAudit(Long investigationId, Long visitId, String action, String reason) {
        this.visitInvestigationId=investigationId; this.visitId=visitId;
        this.action=action; this.reason=reason; this.changedAt=OffsetDateTime.now();
    }
}
