package com.rpscans.api.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpscans.api.dto.ScanReportResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One PostgreSQL statement for the scan reports list; no per-visit database calls. */
@Repository
public class ScanReportFastRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public ScanReportFastRepository(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    private static final String SQL = """
        SELECT v.id, v.visit_date, v.self, p.id AS patient_db_id, p.patient_code,
               p.name AS patient_name, p.father_husband_name, p.phone,
               COALESCE(d.id, rp.id) AS referral_id, COALESCE(d.name, rp.name) AS referral_name,
               COALESCE(d.hospital, rp.hospital) AS hospital_name, COALESCE(d.code, rp.code) AS referral_code,
               COALESCE(v.gross_amount, 0) AS gross_amount,
               COALESCE(v.discount_amount, 0) AS discount_amount,
               COALESCE(v.final_amount, COALESCE(v.gross_amount,0)-COALESCE(v.discount_amount,0)) AS final_amount,
               COALESCE(inv.investigation_names, '') AS investigation_names,
               COALESCE(inv.investigation_json, '[]'::jsonb)::text AS investigation_json,
               COALESCE(inv.has_concession, false) AS has_concession,
               COALESCE(inv.discount_reasons, '') AS discount_reasons,
               COALESCE(pay.payment_json, '[]'::jsonb)::text AS payment_json
        FROM visits v
        JOIN patients p ON p.id = v.patient_id
        LEFT JOIN referral_parties rp ON rp.id = v.referral_party_id
        LEFT JOIN doctors d ON d.id = v.doctor_id
        LEFT JOIN LATERAL (
            SELECT string_agg(DISTINCT i.name, ', ' ORDER BY i.name) FILTER (WHERE vi.cancelled = false) AS investigation_names,
                   jsonb_agg(jsonb_build_object(
                     'name', i.name,
                     'referralCode', COALESCE(vi.referral_code, ''),
                     'cancelled', vi.cancelled,
                     'cancellationReason', vi.cancellation_reason
                   ) ORDER BY vi.id) AS investigation_json,
                   bool_or(vi.concession) FILTER (WHERE vi.cancelled = false) AS has_concession,
                   string_agg(DISTINCT NULLIF(btrim(vi.discount_reason), ''), ', ') FILTER (WHERE vi.cancelled = false) AS discount_reasons
            FROM visit_investigations vi
            JOIN investigations i ON i.id = vi.investigation_id
            WHERE vi.visit_id = v.id
        ) inv ON true
        LEFT JOIN LATERAL (
            SELECT jsonb_agg(jsonb_build_object(
                       'mode', CASE pm.mode WHEN 'UPI' THEN 'UPI' WHEN 'CARD' THEN 'Card' ELSE 'Cash' END,
                       'amount', pm.amount) ORDER BY pm.id) AS payment_json
            FROM payments pm
            WHERE pm.visit_id = v.id
        ) pay ON true
        WHERE v.visit_date BETWEEN ? AND ?
        ORDER BY v.visit_date DESC, v.id DESC
        """;

    public List<ScanReportResponse> find(LocalDate from, LocalDate to) {
        return jdbc.query(SQL, (rs, rowNum) -> {
            List<ScanReportResponse.PaymentInfo> paymentInfo;
            try {
                paymentInfo = json.readValue(rs.getString("payment_json"),
                        new TypeReference<List<ScanReportResponse.PaymentInfo>>() {});
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parse payments for visit " + rs.getLong("id"), e);
            }
            List<ScanReportResponse.InvestigationInfo> investigationInfo;
            try {
                investigationInfo = json.readValue(rs.getString("investigation_json"),
                        new TypeReference<List<ScanReportResponse.InvestigationInfo>>() {});
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parse investigations for visit " + rs.getLong("id"), e);
            }
            String patientCode = rs.getString("patient_code");
            if (patientCode == null || patientCode.isBlank()) {
                patientCode = "RP" + String.format("%06d", rs.getLong("patient_db_id"));
            }
            return new ScanReportResponse(
                    rs.getLong("id"), rs.getObject("visit_date", LocalDate.class),
                    patientCode, rs.getString("patient_name"), rs.getString("father_husband_name"),
                    rs.getString("phone"), rs.getBoolean("self"),
                    rs.getString("investigation_names"), investigationInfo, rs.getString("referral_name") == null ? "" : rs.getString("referral_name"),
                    rs.getString("hospital_name") == null ? "" : rs.getString("hospital_name"),
                    rs.getString("referral_code") == null ? "" : rs.getString("referral_code"),
                    rs.getBoolean("has_concession"), rs.getString("discount_reasons"),
                    rs.getBigDecimal("gross_amount"), rs.getBigDecimal("discount_amount"),
                    rs.getBigDecimal("final_amount"), paymentInfo
            );
        }, from, to);
    }
}
