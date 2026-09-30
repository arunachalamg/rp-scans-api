package com.rpscans.api.controller;

import com.rpscans.api.entity.Doctor;
import com.rpscans.api.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepository repo;

    public DoctorController(DoctorRepository r) {
        repo = r;
    }

    @GetMapping
    public List<Doctor> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Doctor one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @GetMapping("/code/{code}")
    public Doctor getByCode(@PathVariable String code) {
        return repo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @PostMapping("/add")
    public Doctor add(@Valid @RequestBody Doctor x) {
        x.id = null;
        return repo.save(x);
    }

    @PutMapping("/{id}")
    public Doctor update(@PathVariable Long id, @Valid @RequestBody Doctor x) {
        x.id = id;
        return repo.save(x);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
