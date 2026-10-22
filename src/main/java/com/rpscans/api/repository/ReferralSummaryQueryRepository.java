package com.rpscans.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Single aggregate query, excluding cancelled investigations. */
@Repository
public class ReferralSummaryQueryRepository {
    private final JdbcTemplate jdbc;
    public ReferralSummaryQueryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final String SQL = """
        WITH attributed AS (
          SELECT v.id AS visit_id, v.patient_id, v.visit_date,
                 CASE WHEN d.id IS NOT NULL THEN d.id ELSE rp.id END AS party_id,
                 CASE WHEN d.id IS NOT NULL THEN d.name ELSE rp.name END AS party_name,
                 CASE WHEN d.id IS NOT NULL THEN
                   CASE WHEN upper(coalesce(d.person_type, 'DOCTOR')) = 'STAFF' THEN 'Staff' ELSE 'Doctor' END
                   ELSE CASE WHEN upper(coalesce(rp.type, 'DOCTOR')) = 'STAFF' THEN 'Staff' ELSE 'Doctor' END END AS party_type,
                 vi.amount, vi.discount_amount, vi.referral_amount
          FROM visits v
          JOIN visit_investigations vi ON vi.visit_id = v.id AND vi.cancelled = false
          LEFT JOIN doctors d ON d.id = v.doctor_id
          LEFT JOIN referral_parties rp ON rp.id = v.referral_party_id
          WHERE v.visit_date BETWEEN ? AND ?
            AND (d.id IS NOT NULL OR rp.id IS NOT NULL)
        )
        SELECT party_id AS id, party_name AS name, party_type AS type,
               COUNT(DISTINCT patient_id) AS patients,
               COALESCE(SUM(COALESCE(amount,0) - COALESCE(discount_amount,0)),0) AS amount,
               COALESCE(SUM(COALESCE(referral_amount,0)),0) AS referral
        FROM attributed
        GROUP BY party_id, party_name, party_type
        ORDER BY party_name, party_type
        """;

    public List<Map<String, Object>> summary(LocalDate from, LocalDate to) {
        return jdbc.query(SQL, (rs, i) -> Map.<String,Object>of(
            "id", rs.getLong("id"),
            "name", rs.getString("name"),
            "type", rs.getString("type"),
            "patients", rs.getLong("patients"),
            "amount", rs.getBigDecimal("amount"),
            "referral", rs.getBigDecimal("referral")
        ), from, to);
    }

    /** Detail lines use the same attribution and cancelled-study rules as summary. */
    public List<Map<String,Object>> details(long partyId, String partyType, LocalDate from, LocalDate to) {
        String sql = """
          SELECT vi.id AS id, v.id AS visit_id, v.visit_date AS date,
                 p.name AS patient_name, p.age, p.gender,
                 COALESCE(d.hospital, rp.hospital, '') AS hospital,
                 i.name AS investigation,
                 vi.amount - COALESCE(vi.discount_amount,0) AS amount,
                 COALESCE(vi.referral_amount,0) AS referral,
                 vi.concession AS concession,
                 COALESCE(vi.discount_reason,'') AS remarks
          FROM visits v
          JOIN patients p ON p.id=v.patient_id
          JOIN visit_investigations vi ON vi.visit_id=v.id AND vi.cancelled=false
          JOIN investigations i ON i.id=vi.investigation_id
          LEFT JOIN doctors d ON d.id=v.doctor_id
          LEFT JOIN referral_parties rp ON rp.id=v.referral_party_id
          WHERE v.visit_date BETWEEN ? AND ?
            AND (CASE WHEN d.id IS NOT NULL THEN d.id ELSE rp.id END)=?
            AND (CASE WHEN d.id IS NOT NULL THEN
                CASE WHEN upper(coalesce(d.person_type,'DOCTOR'))='STAFF' THEN 'Staff' ELSE 'Doctor' END
                ELSE CASE WHEN upper(coalesce(rp.type,'DOCTOR'))='STAFF' THEN 'Staff' ELSE 'Doctor' END END)=?
          ORDER BY v.visit_date, v.id, vi.id
          """;
        return jdbc.query(sql, (rs,n) -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("id",rs.getLong("id")); m.put("visitId",rs.getLong("visit_id"));
            m.put("date",rs.getDate("date").toLocalDate().toString());
            m.put("patientName",rs.getString("patient_name"));
            m.put("age",rs.getObject("age")); m.put("gender",rs.getString("gender"));
            m.put("hospital",rs.getString("hospital")); m.put("investigation",rs.getString("investigation"));
            m.put("amount",rs.getBigDecimal("amount")); m.put("referral",rs.getBigDecimal("referral"));
            m.put("concession",rs.getBoolean("concession")); m.put("remarks",rs.getString("remarks"));
            return m;
        },from,to,partyId,partyType);
    }
}
