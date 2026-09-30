package com.rpscans.api.dto;
import java.math.BigDecimal; import java.time.LocalDate;
public record VisitResponse(Long id,String visitCode,String patientCode,LocalDate visitDate,BigDecimal grossAmount,BigDecimal totalConcession,BigDecimal netAmount,BigDecimal totalPaid,BigDecimal balanceAmount) {}
