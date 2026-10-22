package com.rpscans.api.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

/** Monthly study counts for the PNDT/JDA report. Counts completed, non-cancelled ultrasound investigation rows. */
@RestController
@RequestMapping("/api/reports/jda-monthly")
public class JdaMonthlyReportController {
    private final JdbcTemplate jdbc;
    public JdaMonthlyReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public Map<String, Object> report(@RequestParam String month) {
        if (!month.matches("\\d{4}-(0[1-9]|1[0-2])")) throw new IllegalArgumentException("month must be YYYY-MM");
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to = from.plusMonths(1);
        String sql = """
          SELECT i.id AS investigation_id, i.code, i.name, i.modality, COUNT(*) AS studies
          FROM visit_investigations vi
          JOIN investigations i ON i.id = vi.investigation_id
          JOIN visits v ON v.id = vi.visit_id
          WHERE v.visit_date >= ? AND v.visit_date < ?
            AND vi.cancelled = false
            AND (lower(i.modality) LIKE '%ultra%' OR lower(i.modality) LIKE '%usg%' OR lower(i.modality) LIKE '%sono%')
          GROUP BY i.id, i.code, i.name, i.modality
          ORDER BY i.name
        """;
        List<Map<String,Object>> studies = jdbc.query(sql, (rs, n) -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("investigationId", rs.getLong("investigation_id"));
            m.put("code", rs.getString("code"));
            m.put("name", rs.getString("name"));
            m.put("modality", rs.getString("modality"));
            m.put("count", rs.getLong("studies"));
            return m;
        }, from, to);
        return Map.of("month", month, "studies", studies);
    }
}
