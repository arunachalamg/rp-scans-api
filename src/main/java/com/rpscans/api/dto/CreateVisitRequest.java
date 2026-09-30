package com.rpscans.api.dto;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
public record CreateVisitRequest(Long patientId, LocalDate visitDate, Long referralPartyId, String remarks, List<Item> investigations, List<Pay> payments) {
 public record Item(Long investigationId, BigDecimal amount, boolean concession, BigDecimal discountAmount, String discountReason, BigDecimal referralAmount){}
 public record Pay(String mode, BigDecimal amount, String referenceNo){}
}
