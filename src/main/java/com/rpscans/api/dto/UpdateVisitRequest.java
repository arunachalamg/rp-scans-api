package com.rpscans.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateVisitRequest(
        LocalDate visitDate, Boolean self, Long referralPartyId, Long doctorId, String remarks,
        List<Item> investigations, List<Pay> payments
) {
    public record Item(
            Long id, Long investigationId, BigDecimal amount, boolean concession,
            BigDecimal discountAmount, String discountReason, BigDecimal referralAmount
    ) {}
    public record Pay(Long id, String mode, BigDecimal amount, String referenceNo) {}
}
