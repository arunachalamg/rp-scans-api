package com.rpscans.api.controller;

import com.rpscans.api.entity.Investigation;
import com.rpscans.api.repository.InvestigationRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/investigations")
public class InvestigationController {
    private final InvestigationRepository repo;

    public InvestigationController(InvestigationRepository r) {
        repo = r;
    }

    @GetMapping
    public List<Investigation> all() {
        return repo.findAll();
    }

    @PostMapping
    public Investigation add(@Valid @RequestBody Investigation x) {
        x.id = null;
        return repo.save(x);
    }

    @PutMapping("/{id}")
    public Investigation update(@PathVariable Long id, @Valid @RequestBody Investigation x) {
        x.id = id;
        return repo.save(x);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
