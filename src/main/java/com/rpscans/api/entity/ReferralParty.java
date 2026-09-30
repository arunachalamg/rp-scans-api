package com.rpscans.api.entity;
import jakarta.persistence.*;
@Entity @Table(name="referral_parties") public class ReferralParty {
 public enum Type { DOCTOR, STAFF }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,unique=true) public String code;
 @Column(nullable=false) public String name; @Enumerated(EnumType.STRING) @Column(nullable=false) public Type type;
 public String hospital; public String phone; @Column(nullable=false) public boolean active=true;
}
