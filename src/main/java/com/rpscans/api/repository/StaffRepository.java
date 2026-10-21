package com.rpscans.api.repository;
import com.rpscans.api.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<Staff,Long> {
 boolean existsByStaffId(String staffId);
 Optional<Staff> findByUsernameIgnoreCase(String username);
}
