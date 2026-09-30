package com.rpscans.api.service;

import com.rpscans.api.dto.*;
import com.rpscans.api.entity.Patient;
import com.rpscans.api.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PatientService {
    private final PatientRepository repo;

    public PatientService(PatientRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public PatientResponse create(PatientRequest r) {
        Patient p = new Patient();
        p.setName(r.name().trim());
        p.setFatherHusbandName(r.fatherHusbandName());
        p.setAge(r.age());
        p.setGender(r.gender());
        p.setPhone(r.phone());
        p.setEmail(r.email());
        p.setAddress(r.address());
        p = repo.save(p);
        p.setPatientCode("RP" + String.format("%06d", p.getId()));
        p = repo.save(p);
        return map(p);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> search(String q) {
       // return repo.search(q).stream().map(this::map).toList();
        return null;
    }

    @Transactional(readOnly = true)
    public PatientResponse get(Long id) {
        return map(repo.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found")));
    }

    private PatientResponse map(Patient p) {
        return new PatientResponse(p.getId(), p.getPatientCode(), p.getName(), p.getFatherHusbandName(), p.getAge(), p.getGender(), p.getPhone(), p.getEmail(), p.getAddress());
    }
}
