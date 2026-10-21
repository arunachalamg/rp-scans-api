package com.rpscans.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PatientDetailedReportResponse(
        Long visitId, LocalDate date,
        Long patientDbId, String patientId, String patientName, String fatherName,
        Integer age, String gender, String phone, String email, String address,
        Long referralPartyId, Long doctorId, boolean self, String referredBy, String hospitalName, String referralCode,
        String remarks, BigDecimal amount, BigDecimal discountAmount, BigDecimal finalAmount,
        List<InvestigationInfo> investigations, List<PaymentInfo> payments
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvestigationInfo(
            Long id, Long investigationId, String code, String name, String modality,
            BigDecimal amount, boolean concession, BigDecimal discountAmount,
            String discountReason, BigDecimal referralAmount, String referralCode, boolean cancelled, String cancellationReason, java.time.OffsetDateTime cancelledAt
    ) {}
    public record PaymentInfo(Long id, String mode, BigDecimal amount, String referenceNo) {}
}
