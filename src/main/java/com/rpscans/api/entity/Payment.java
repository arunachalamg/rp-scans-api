package com.rpscans.api.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*;
@Entity @Table(name="payments") public class Payment {
 public enum Mode { CASH, UPI, CARD }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne(optional=false) public Visit visit;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public Mode mode; @Column(nullable=false,precision=12,scale=2) public BigDecimal amount;
 @Column(nullable=false) public LocalDateTime paidAt=LocalDateTime.now(); public String referenceNo;
}
