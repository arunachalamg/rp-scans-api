package com.rpscans.api.repository;

import com.rpscans.api.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    @Query("""
        select v from Visit v
        join fetch v.patient p
        left join fetch v.referralParty rp
        where v.visitDate between :fromDate and :toDate
        order by v.visitDate desc, v.id desc
    """)
    List<Visit> findReportVisits(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
    @Query("""
        select v from Visit v
        join fetch v.patient p
        left join fetch v.referralParty rp
        where p.patientCode = :patientCode
        order by v.visitDate desc, v.id desc
    """)
    List<Visit> findReportVisitsByPatientCode(
            @Param("patientCode") String patientCode);

}
