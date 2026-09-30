package com.rpscans.api.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record PaymentRequest(@NotBlank String mode,@NotNull @DecimalMin(value="0.01") BigDecimal amount) {}
