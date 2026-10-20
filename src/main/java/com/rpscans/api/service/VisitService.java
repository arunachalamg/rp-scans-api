package com.rpscans.api.service;

import com.rpscans.api.dto.CreateVisitRequest;
import com.rpscans.api.dto.UpdateVisitRequest;
import com.rpscans.api.entity.*;
import com.rpscans.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class VisitService {
    private final PatientRepository patients;
    private final ReferralPartyRepository refs;
    private final DoctorRepository doctors;
    private final InvestigationRepository invs;
    private final VisitRepository visits;
    private final VisitInvestigationRepository items;
    private final PaymentRepository payments;

    public VisitService(PatientRepository a, ReferralPartyRepository b, InvestigationRepository c,
                        VisitRepository d, VisitInvestigationRepository e, PaymentRepository f, DoctorRepository g) {
        patients=a; refs=b; invs=c; visits=d; items=e; payments=f; doctors=g;
    }

    @Transactional
    public Visit create(CreateVisitRequest r) {
        Visit v=new Visit();
        v.patient=patients.findById(r.patientId()).orElseThrow();
        v.visitDate=r.visitDate(); v.remarks=r.remarks();
        if(r.referralPartyId()!=null) v.referralParty=refs.findById(r.referralPartyId()).orElseThrow();
        if(r.doctorId()!=null) { v.doctor=doctors.findById(r.doctorId()).orElseThrow(); v.referralParty=null; }
        applyTotals(v, r.investigations());
        v=visits.save(v);
        if(r.investigations()!=null) for(var q:r.investigations()) {
            VisitInvestigation x=new VisitInvestigation(); x.visit=v;
            x.investigation=invs.findById(q.investigationId()).orElseThrow(); x.amount=nvl(q.amount());
            x.concession=q.concession(); x.discountAmount=nvl(q.discountAmount()); x.discountReason=q.discountReason();
            x.referralAmount=nvl(q.referralAmount()); items.save(x);
        }
        if(r.payments()!=null) for(var q:r.payments()) {
            Payment p=new Payment(); p.visit=v; p.mode=parseMode(q.mode()); p.amount=nvl(q.amount());
            p.referenceNo=q.referenceNo(); payments.save(p);
        }
        return v;
    }

    @Transactional
    public Visit update(Long visitId, UpdateVisitRequest r) {
        Visit v=visits.findById(visitId).orElseThrow();
        if(r.visitDate()!=null) v.visitDate=r.visitDate();
        if(r.self()!=null) v.self=r.self();

        // Do not accidentally clear referral during an edit.
        // Referral is cleared only when the UI explicitly marks the visit as Self.
        if (r.doctorId() != null) {
            v.doctor = doctors.findById(r.doctorId()).orElseThrow();
            v.referralParty = null;
        } else if (r.referralPartyId() != null) {
            v.doctor = null;
            v.referralParty = refs.findById(r.referralPartyId()).orElseThrow();
        }
        v.remarks=r.remarks();

        // Fetch existing children once per table instead of querying each row by ID.
        Map<Long, VisitInvestigation> existingItems = new HashMap<>();
        for (VisitInvestigation item : items.findByVisitId(v.id)) existingItems.put(item.id, item);
        Set<Long> keptItems = new HashSet<>();
        BigDecimal gross = BigDecimal.ZERO, discount = BigDecimal.ZERO;
        if (r.investigations() != null) {
            for (var q : r.investigations()) {
                VisitInvestigation x;
                if (q.id() != null) {
                    x = existingItems.get(q.id());
                    if (x == null) throw new IllegalArgumentException("Investigation does not belong to visit");
                    if (!keptItems.add(q.id())) throw new IllegalArgumentException("Duplicate investigation row ID");
                } else {
                    x = new VisitInvestigation(); x.visit = v;
                }
                // A reference avoids SELECTing investigation master for each billing row.
                x.investigation = invs.getReferenceById(q.investigationId());
                x.amount = nvl(q.amount()); x.concession = q.concession();
                x.discountAmount = q.concession() ? nvl(q.discountAmount()) : BigDecimal.ZERO;
                x.discountReason = q.concession() ? q.discountReason() : null;
                x.referralAmount = nvl(q.referralAmount());
                if (q.id() == null) items.save(x);
                gross = gross.add(x.amount); discount = discount.add(x.discountAmount);
            }
            for (var old : existingItems.values()) if (!keptItems.contains(old.id)) items.delete(old);
            v.grossAmount = gross; v.discountAmount = discount; v.finalAmount = gross.subtract(discount);
        }

        Map<Long, Payment> existingPayments = new HashMap<>();
        for (Payment pay : payments.findByVisitId(v.id)) existingPayments.put(pay.id, pay);
        Set<Long> keptPayments = new HashSet<>();
        if (r.payments() != null) {
            for (var q : r.payments()) {
                Payment pay;
                if (q.id() != null) {
                    pay = existingPayments.get(q.id());
                    if (pay == null) throw new IllegalArgumentException("Payment does not belong to visit");
                    if (!keptPayments.add(q.id())) throw new IllegalArgumentException("Duplicate payment row ID");
                } else { pay = new Payment(); pay.visit = v; }
                pay.mode = parseMode(q.mode()); pay.amount = nvl(q.amount()); pay.referenceNo = q.referenceNo();
                if (q.id() == null) payments.save(pay);
            }
            for (var old : existingPayments.values()) if (!keptPayments.contains(old.id)) payments.delete(old);
        }
        // Existing entities are managed; JPA flushes changed fields on transaction commit.
        return v;
    }

    private void applyTotals(Visit v, List<CreateVisitRequest.Item> list) {
        BigDecimal gross=BigDecimal.ZERO, disc=BigDecimal.ZERO;
        if(list!=null) for(var q:list) { gross=gross.add(nvl(q.amount())); disc=disc.add(nvl(q.discountAmount())); }
        v.grossAmount=gross; v.discountAmount=disc; v.finalAmount=gross.subtract(disc);
    }
    private BigDecimal nvl(BigDecimal x){ return x==null?BigDecimal.ZERO:x; }
    private Payment.Mode parseMode(String mode){ return Payment.Mode.valueOf((mode==null?"CASH":mode).toUpperCase()); }
}
