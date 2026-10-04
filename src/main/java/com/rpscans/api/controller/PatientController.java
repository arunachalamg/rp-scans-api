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

    @PostMapping("/add")
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
        Patient existing = repo.findById(id).orElseThrow();

        // Preserve the permanent RP patient code during edit.
        // Saving the incoming detached object directly was clearing patientCode
        // because the edit payload contains only editable patient fields.
        existing.name = x.name;
        existing.fatherHusbandName = x.fatherHusbandName;
        existing.age = x.age;
        existing.gender = x.gender;
        existing.phone = x.phone;
        existing.email = x.email;
        existing.address = x.address;

        // Also repairs patients whose code was cleared by the old update logic.
        if (existing.patientCode == null || existing.patientCode.isBlank()) {
            existing.patientCode = "RP" + String.format("%06d", existing.id);
        }
        return repo.save(existing);
    }
}
