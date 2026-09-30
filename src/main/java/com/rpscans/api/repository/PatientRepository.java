package com.rpscans.api.repository;

import com.rpscans.api.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
 java.util.List<Patient> findTop20ByNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrPatientCodeContainingIgnoreCase(String name, String phone, String patientCode);
}
