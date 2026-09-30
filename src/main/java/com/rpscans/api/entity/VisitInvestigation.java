package com.rpscans.api.entity;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="visit_investigations") public class VisitInvestigation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne(optional=false) public Visit visit; @ManyToOne(optional=false) public Investigation investigation;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal amount; @Column(nullable=false) public boolean concession=false;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal discountAmount=BigDecimal.ZERO; public String discountReason;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal referralAmount=BigDecimal.ZERO;
}
