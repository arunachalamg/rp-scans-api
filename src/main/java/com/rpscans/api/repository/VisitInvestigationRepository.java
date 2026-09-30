package com.rpscans.api.repository;
import com.rpscans.api.entity.VisitInvestigation; import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitInvestigationRepository extends JpaRepository<VisitInvestigation,Long> {
 java.util.List<VisitInvestigation> findByVisitId(Long visitId);
}
