package com.rpscans.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ScanReportResponse(
        Long id,
        LocalDate date,
        String patientId,
        String patientName,
        String fatherName,
        String phone,
        boolean self,
        String investigation,
        String referredBy,
        String hospitalName,
        String referralCode,
        boolean concession,
        String discountReason,
        BigDecimal amount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        List<PaymentInfo> payments
) {
    public record PaymentInfo(String mode, BigDecimal amount) {}
}
