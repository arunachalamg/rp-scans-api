package com.rpscans.api.controller;

import com.rpscans.api.repository.ReferralSummaryQueryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/referral-summary")
public class ReferralSummaryController {
    private final ReferralSummaryQueryRepository repository;
    public ReferralSummaryController(ReferralSummaryQueryRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Map<String, Object>> summary(@RequestParam String period, @RequestParam String date) {
        try {
            LocalDate from;
            LocalDate to;
            switch (period.toUpperCase(java.util.Locale.ROOT)) {
                case "DAY" -> { from = LocalDate.parse(date); to = from; }
                case "MONTH" -> { YearMonth month = YearMonth.parse(date); from = month.atDay(1); to = month.atEndOfMonth(); }
                case "YEAR" -> { int year = Integer.parseInt(date); from = LocalDate.of(year, 1, 1); to = LocalDate.of(year, 12, 31); }
                default -> throw new IllegalArgumentException("Unsupported period");
            }
            return repository.summary(from, to);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period or date", ex);
        }
    }

    @GetMapping("/{id}/details")
    public List<Map<String,Object>> details(@PathVariable long id,
        @RequestParam String type, @RequestParam String period, @RequestParam String date) {
        if (!type.equals("Doctor") && !type.equals("Staff"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid referral type");
        try {
            LocalDate from, to;
            switch(period.toUpperCase(java.util.Locale.ROOT)) {
                case "DAY" -> { from=LocalDate.parse(date); to=from; }
                case "MONTH" -> { YearMonth m=YearMonth.parse(date); from=m.atDay(1); to=m.atEndOfMonth(); }
                case "YEAR" -> { int y=Integer.parseInt(date); from=LocalDate.of(y,1,1); to=LocalDate.of(y,12,31); }
                default -> throw new IllegalArgumentException("Invalid period");
            }
            return repository.details(id,type,from,to);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid period or date",ex);
        }
    }
}
