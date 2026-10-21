package com.rpscans.api.controller;

import com.rpscans.api.repository.DashboardQueryRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardQueryRepository repository;
    public DashboardController(DashboardQueryRepository repository) { this.repository = repository; }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public String summary() { return repository.summary(); }
}
