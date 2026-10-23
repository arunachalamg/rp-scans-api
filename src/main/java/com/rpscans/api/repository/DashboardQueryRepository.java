package com.rpscans.api.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.ZoneId;

@Repository
public class DashboardQueryRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public DashboardQueryRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    // Single database round-trip; payment aggregation is separate to avoid multiplying
    // payments by investigations. "Pending" means outstanding on visits dated today.
    private static final String SQL = """
      WITH today_visits AS (
        SELECT v.id, v.patient_id, v.final_amount, v.discount_amount
        FROM visits v WHERE v.visit_date = :today
      ), today_payments AS (
        SELECT pm.visit_id, SUM(pm.amount) AS paid
        FROM payments pm JOIN today_visits v ON v.id = pm.visit_id
        GROUP BY pm.visit_id
      ), visit_totals AS (
        SELECT COUNT(*) AS patients,
          COALESCE(SUM(GREATEST(v.final_amount - COALESCE(p.paid,0),0)),0) AS pending,
          COUNT(*) FILTER (WHERE v.final_amount > COALESCE(p.paid,0)) AS pending_bills,
          COALESCE(SUM(v.discount_amount),0) AS discount
        FROM today_visits v LEFT JOIN today_payments p ON p.visit_id=v.id
      ), new_patients AS (
        SELECT COUNT(*) AS total FROM patients WHERE created_at >= :dayStart AND created_at < :dayEnd
      ), daily_investigations AS (
        SELECT COUNT(*) AS total FROM visit_investigations vi JOIN today_visits v ON v.id=vi.visit_id
      ), modality_counts AS (
        SELECT CASE
          WHEN UPPER(i.modality) LIKE '%ULTRA%' OR UPPER(i.modality) LIKE '%USG%' OR UPPER(i.modality) LIKE '%DOPPLER%' THEN 'Ultrasound'
          WHEN UPPER(i.modality) LIKE '%LAB%' OR UPPER(i.modality) LIKE '%PATH%' THEN 'Laboratory'
          WHEN UPPER(i.modality) LIKE '%X%RAY%' THEN 'X-Ray'
          WHEN UPPER(i.modality) LIKE '%ECHO%' OR UPPER(i.modality) LIKE '%ECG%' THEN 'ECHO / ECG'
          ELSE 'Other' END AS label, COUNT(*) AS count
        FROM visit_investigations vi JOIN today_visits v ON v.id=vi.visit_id
        JOIN investigations i ON i.id=vi.investigation_id
        GROUP BY 1
      ), payment_modes AS (
        SELECT UPPER(pm.mode) AS mode, SUM(pm.amount) AS amount
        FROM payments pm WHERE pm.paid_at >= :dayStart AND pm.paid_at < :dayEnd
        GROUP BY UPPER(pm.mode)
      ), recent AS (
        SELECT v.id, p.patient_code, p.name, v.visit_date, v.created_at, v.final_amount,
          COALESCE((SELECT string_agg(z.name, ', ' ORDER BY z.id)
            FROM (SELECT i.id, i.name FROM visit_investigations vi
              JOIN investigations i ON i.id=vi.investigation_id
              WHERE vi.visit_id=v.id LIMIT 4) z),'—') AS test,
          COALESCE((SELECT SUM(pm.amount) FROM payments pm WHERE pm.visit_id=v.id),0) AS paid
        FROM visits v JOIN patients p ON p.id=v.patient_id
        ORDER BY v.visit_date DESC, v.id DESC LIMIT 8
      )
      SELECT jsonb_build_object(
        'todayPatients', (SELECT patients FROM visit_totals),
        'newPatients', (SELECT total FROM new_patients),
        'todayCollection', COALESCE((SELECT SUM(amount) FROM payment_modes),0),
        'investigations', (SELECT total FROM daily_investigations),
        'pendingAmount', (SELECT pending FROM visit_totals),
        'pendingBills', (SELECT pending_bills FROM visit_totals),
        'modalities', COALESCE((SELECT jsonb_agg(jsonb_build_object('label',label,'count',count)) FROM modality_counts),'[]'::jsonb),
        'payments', jsonb_build_object(
          'cash',COALESCE((SELECT amount FROM payment_modes WHERE mode='CASH'),0),
          'upi',COALESCE((SELECT amount FROM payment_modes WHERE mode='UPI'),0),
          'card',COALESCE((SELECT amount FROM payment_modes WHERE mode='CARD'),0),
          'discount',(SELECT discount FROM visit_totals)),
        'recentPatients',COALESCE((SELECT jsonb_agg(jsonb_build_object(
          'id',COALESCE(NULLIF(patient_code,''),'RP' || lpad(id::text,6,'0')),
          'name',name,'test',test,'time',to_char(created_at,'HH12:MI AM'),
          'amount',final_amount,'status',CASE WHEN paid>=final_amount THEN 'Paid' ELSE 'Due' END
        ) ORDER BY visit_date DESC,id DESC) FROM recent),'[]'::jsonb)
      )::text
      """;

    public String summary() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return jdbc.queryForObject(SQL, new MapSqlParameterSource("today", today)
            .addValue("dayStart", today.atStartOfDay())
            .addValue("dayEnd", today.plusDays(1).atStartOfDay()), String.class);
    }
}
