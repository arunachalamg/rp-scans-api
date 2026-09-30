package com.rpscans.api.entity;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import java.math.BigDecimal;
@Entity @Table(name="investigations") public class Investigation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank @Column(nullable=false,unique=true) public String code; @NotBlank @Column(nullable=false) public String name;
 @NotBlank @Column(nullable=false) public String modality; @NotNull @PositiveOrZero @Column(nullable=false,precision=12,scale=2) public BigDecimal amount;
 @Column(nullable=false) public boolean active=true;
}
