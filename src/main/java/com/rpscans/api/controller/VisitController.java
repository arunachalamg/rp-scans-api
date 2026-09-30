package com.rpscans.api.controller;

import com.rpscans.api.dto.CreateVisitRequest;
import com.rpscans.api.entity.Visit;
import com.rpscans.api.repository.VisitRepository;
import com.rpscans.api.service.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/visits")
public class VisitController {
    private final VisitRepository repo;
    private final VisitService service;

    public VisitController(VisitRepository r, VisitService s) {
        repo = r;
        service = s;
    }

    @GetMapping
    public List<Visit> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Visit one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Visit create(@RequestBody CreateVisitRequest r) {
        return service.create(r);
    }
}
