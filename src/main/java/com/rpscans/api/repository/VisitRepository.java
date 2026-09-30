package com.rpscans.api.repository;
import com.rpscans.api.entity.Visit; import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitRepository extends JpaRepository<Visit,Long> {
}
