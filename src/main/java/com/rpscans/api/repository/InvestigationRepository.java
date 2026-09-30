package com.rpscans.api.repository;
import com.rpscans.api.entity.Investigation; import org.springframework.data.jpa.repository.JpaRepository;
public interface InvestigationRepository extends JpaRepository<Investigation,Long> {
}
