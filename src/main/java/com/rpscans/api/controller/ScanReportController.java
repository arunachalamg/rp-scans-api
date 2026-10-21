package com.rpscans.api.controller;

import com.rpscans.api.dto.ScanReportResponse;
import com.rpscans.api.dto.PatientDetailedReportResponse;
import com.rpscans.api.entity.Payment;
import com.rpscans.api.entity.Visit;
import com.rpscans.api.repository.PaymentRepository;
import com.rpscans.api.repository.VisitInvestigationRepository;
import com.rpscans.api.repository.VisitRepository;
import com.rpscans.api.repository.ScanReportFastRepository;
import com.rpscans.api.repository.PatientReportQueryRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/reports/scans")
public class ScanReportController {
    private final VisitRepository visits;
    private final ScanReportFastRepository fastReports;
    private final PatientReportQueryRepository patientReportQueries;
    private final VisitInvestigationRepository investigations;
    private final PaymentRepository payments;

    public ScanReportController(
            VisitRepository visits,
            ScanReportFastRepository fastReports,
            PatientReportQueryRepository patientReportQueries,
            VisitInvestigationRepository investigations,
            PaymentRepository payments) {
        this.visits = visits;
        this.fastReports = fastReports;
        this.patientReportQueries = patientReportQueries;
        this.investigations = investigations;
        this.payments = payments;
    }

    @GetMapping
    public List<ScanReportResponse> report(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return fastReports.find(from, to);
    }

    @GetMapping("/patients/{patientCode}")
    public List<PatientDetailedReportResponse> patientReport(@PathVariable String patientCode) {
        return patientReportQueries.findByPatientCode(patientCode);
    }

    private PatientDetailedReportResponse toDetailedResponse(Visit visit) {
        var patient = visit.patient;
        var referral = visit.referralParty;
        var itemList = investigations.findByVisitId(visit.id).stream().map(item ->
                new PatientDetailedReportResponse.InvestigationInfo(
                        item.id, item.investigation.id, item.investigation.code, item.investigation.name,
                        item.investigation.modality, item.amount, item.concession, item.discountAmount,
                        item.discountReason, item.referralAmount, item.referralCode, item.cancelled, item.cancellationReason, item.cancelledAt)).toList();
        var paymentList = payments.findByVisitId(visit.id).stream().map(p ->
                new PatientDetailedReportResponse.PaymentInfo(p.id, formatMode(p.mode), p.amount, p.referenceNo)).toList();
        BigDecimal gross = visit.grossAmount == null ? BigDecimal.ZERO : visit.grossAmount;
        BigDecimal discount = visit.discountAmount == null ? BigDecimal.ZERO : visit.discountAmount;
        BigDecimal net = visit.finalAmount == null ? gross.subtract(discount) : visit.finalAmount;
        String patientCode = patient.patientCode == null || patient.patientCode.isBlank()
                ? "RP" + String.format("%06d", patient.id)
                : patient.patientCode;
        return new PatientDetailedReportResponse(
                visit.id, visit.visitDate, patient.id, patientCode, patient.name, patient.fatherHusbandName,
                patient.age, patient.gender, patient.phone, patient.email, patient.address,
                referral == null ? null : referral.id, visit.doctor == null ? null : visit.doctor.id, visit.self,
                visit.doctor != null ? visit.doctor.name : (referral == null ? "" : referral.name), visit.doctor != null ? visit.doctor.hospital : (referral == null || referral.hospital == null ? "" : referral.hospital),
                visit.doctor != null ? visit.doctor.code : (referral == null ? "" : referral.code), visit.remarks, gross, discount, net, itemList, paymentList);
    }

    private ScanReportResponse toResponse(Visit visit) {
        var patient = visit.patient;
        var referral = visit.referralParty;
        var items = investigations.findByVisitId(visit.id);

        String investigationNames = items.stream()
                .map(item -> item.investigation.name)
                .filter(Objects::nonNull)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        boolean concession = items.stream().anyMatch(item -> item.concession);
        String discountReason = items.stream()
                .map(item -> item.discountReason)
                .filter(reason -> reason != null && !reason.isBlank())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        List<ScanReportResponse.PaymentInfo> paymentList = payments.findByVisitId(visit.id)
                .stream()
                .map(p -> new ScanReportResponse.PaymentInfo(formatMode(p.mode), p.amount))
                .toList();

        BigDecimal gross = visit.grossAmount == null ? BigDecimal.ZERO : visit.grossAmount;
        BigDecimal discount = visit.discountAmount == null ? BigDecimal.ZERO : visit.discountAmount;
        BigDecimal net = visit.finalAmount == null ? gross.subtract(discount) : visit.finalAmount;

        String patientCode = patient.patientCode == null || patient.patientCode.isBlank()
                ? "RP" + String.format("%06d", patient.id)
                : patient.patientCode;

        return new ScanReportResponse(
                visit.id,
                visit.visitDate,
                patientCode,
                patient.name,
                patient.fatherHusbandName,
                patient.phone,
                visit.self,
                investigationNames,
                items.stream().map(item ->
                        new ScanReportResponse.InvestigationInfo(
                                item.investigation.name,
                                item.referralCode == null ? "" : item.referralCode,
                                item.cancelled,
                                item.cancellationReason
                        )
                ).toList(),
                referral == null ? "" : referral.name,
                referral == null || referral.hospital == null ? "" : referral.hospital,
                referral == null ? "" : referral.code,
                concession,
                discountReason,
                gross,
                discount,
                net,
                paymentList
        );
    }

    private String formatMode(Payment.Mode mode) {
        if (mode == Payment.Mode.UPI) return "UPI";
        if (mode == Payment.Mode.CARD) return "Card";
        return "Cash";
    }
}
