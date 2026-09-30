package com.rpscans.api.entity;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="visits") public class Visit {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne(optional=false) public Patient patient;
 @Column(nullable=false) public LocalDate visitDate; @ManyToOne public ReferralParty referralParty;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal grossAmount=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal discountAmount=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal finalAmount=BigDecimal.ZERO;
 public String remarks;
}
