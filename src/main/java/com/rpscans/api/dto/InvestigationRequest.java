package com.rpscans.api.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record InvestigationRequest(@NotBlank String modality,@NotBlank String testName,@NotNull @DecimalMin("0.0") BigDecimal amount,String referralCode,boolean concession,@DecimalMin("0.0") BigDecimal discountAmount,String discountReason) {}
