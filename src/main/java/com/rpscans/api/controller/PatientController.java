package com.rpscans.api.controller;

import com.rpscans.api.entity.Patient;
import com.rpscans.api.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository repo;

    public PatientController(PatientRepository r) {
        repo = r;
    }

    @GetMapping
    public List<Patient> search(@RequestParam(defaultValue = "") String query) {
        if (query.isBlank()) return repo.findAll();
        return repo.findTop20ByNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrPatientCodeContainingIgnoreCase(query, query, query);
    }

    @GetMapping("/{id}")
    public Patient one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Patient add(@Valid @RequestBody Patient x) {
        x.id = null;
        Patient p = repo.save(x);
        if (p.patientCode == null) {
            p.patientCode = "RP" + String.format("%06d", p.id);
            p = repo.save(p);
        }
        return p;
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @Valid @RequestBody Patient x) {
        x.id = id;
        return repo.save(x);
    }
}
