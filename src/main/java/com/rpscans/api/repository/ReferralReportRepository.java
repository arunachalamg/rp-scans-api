package com.rpscans.api.repository;
import com.rpscans.api.entity.VisitInvestigation; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.LocalDate; import java.util.List;
public interface ReferralReportRepository extends JpaRepository<VisitInvestigation,Long> {
 @Query(value="SELECT rp.id, rp.name, rp.type, COUNT(DISTINCT v.id) total_patients, COALESCE(SUM(vi.amount-vi.discount_amount),0) total_amount, COALESCE(SUM(vi.referral_amount),0) total_referral_amount FROM referral_parties rp JOIN visits v ON v.referral_party_id=rp.id JOIN visit_investigations vi ON vi.visit_id=v.id WHERE v.visit_date BETWEEN :fromDate AND :toDate GROUP BY rp.id,rp.name,rp.type ORDER BY rp.name",nativeQuery=true)
 List<Object[]> summary(@Param("fromDate") LocalDate fromDate,@Param("toDate") LocalDate toDate);
}
