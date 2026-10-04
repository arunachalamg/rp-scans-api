package com.rpscans.api.service;

import com.rpscans.api.dto.CreateVisitRequest;
import com.rpscans.api.dto.UpdateVisitRequest;
import com.rpscans.api.entity.*;
import com.rpscans.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
public class VisitService {
    private final PatientRepository patients;
    private final ReferralPartyRepository refs;
    private final InvestigationRepository invs;
    private final VisitRepository visits;
    private final VisitInvestigationRepository items;
    private final PaymentRepository payments;

    public VisitService(PatientRepository a, ReferralPartyRepository b, InvestigationRepository c,
                        VisitRepository d, VisitInvestigationRepository e, PaymentRepository f) {
        patients=a; refs=b; invs=c; visits=d; items=e; payments=f;
    }

    @Transactional
    public Visit create(CreateVisitRequest r) {
        Visit v=new Visit();
        v.patient=patients.findById(r.patientId()).orElseThrow();
        v.visitDate=r.visitDate(); v.remarks=r.remarks();
        if(r.referralPartyId()!=null) v.referralParty=refs.findById(r.referralPartyId()).orElseThrow();
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

        // Do not accidentally clear referral during an edit.
        // Referral is cleared only when the UI explicitly marks the visit as Self.
        if (Boolean.TRUE.equals(r.self())) {
            v.referralParty = null;
        } else if (r.referralPartyId() != null) {
            v.referralParty = refs.findById(r.referralPartyId()).orElseThrow();
        }
        v.remarks=r.remarks();

        Set<Long> keptItems=new HashSet<>();
        BigDecimal gross=BigDecimal.ZERO, discount=BigDecimal.ZERO;
        if(r.investigations()!=null) for(var q:r.investigations()) {
            VisitInvestigation x;
            if(q.id()!=null) {
                x=items.findById(q.id()).orElseThrow();
                if(!x.visit.id.equals(v.id)) throw new IllegalArgumentException("Investigation does not belong to visit");
            } else { x=new VisitInvestigation(); x.visit=v; }
            x.investigation=invs.findById(q.investigationId()).orElseThrow(); x.amount=nvl(q.amount());
            x.concession=q.concession(); x.discountAmount=nvl(q.discountAmount()); x.discountReason=q.discountReason();
            x.referralAmount=nvl(q.referralAmount()); x=items.save(x); keptItems.add(x.id);
            gross=gross.add(x.amount); discount=discount.add(x.discountAmount);
        }
        for(var old:items.findByVisitId(v.id)) if(!keptItems.contains(old.id)) items.delete(old);
        v.grossAmount=gross; v.discountAmount=discount; v.finalAmount=gross.subtract(discount); visits.save(v);

        Set<Long> keptPayments=new HashSet<>();
        if(r.payments()!=null) for(var q:r.payments()) {
            Payment p;
            if(q.id()!=null) {
                p=payments.findById(q.id()).orElseThrow();
                if(!p.visit.id.equals(v.id)) throw new IllegalArgumentException("Payment does not belong to visit");
            } else { p=new Payment(); p.visit=v; }
            p.mode=parseMode(q.mode()); p.amount=nvl(q.amount()); p.referenceNo=q.referenceNo();
            p=payments.save(p); keptPayments.add(p.id);
        }
        for(var old:payments.findByVisitId(v.id)) if(!keptPayments.contains(old.id)) payments.delete(old);
        return v;
    }

    private void applyTotals(Visit v, java.util.List<CreateVisitRequest.Item> list) {
        BigDecimal gross=BigDecimal.ZERO, disc=BigDecimal.ZERO;
        if(list!=null) for(var q:list) { gross=gross.add(nvl(q.amount())); disc=disc.add(nvl(q.discountAmount())); }
        v.grossAmount=gross; v.discountAmount=disc; v.finalAmount=gross.subtract(disc);
    }
    private BigDecimal nvl(BigDecimal x){ return x==null?BigDecimal.ZERO:x; }
    private Payment.Mode parseMode(String mode){ return Payment.Mode.valueOf((mode==null?"CASH":mode).toUpperCase()); }
}
