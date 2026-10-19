package com.rpscans.api.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpscans.api.dto.PatientDetailedReportResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/** One PostgreSQL statement returns the entire visit history, with independently aggregated children. */
@Repository
public class PatientReportQueryRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public PatientReportQueryRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    private static final String SQL = """
        SELECT COALESCE(jsonb_agg(
          jsonb_build_object(
            'visitId', v.id, 'date', v.visit_date,
            'patientDbId', p.id,
            'patientId', COALESCE(NULLIF(p.patient_code, ''), 'RP' || lpad(p.id::text, 6, '0')),
            'patientName', p.name, 'fatherName', p.father_husband_name,
            'age', p.age, 'gender', p.gender, 'phone', p.phone,
            'email', p.email, 'address', p.address,
            'referralPartyId', rp.id,
            'self', (rp.id IS NULL),
            'referredBy', COALESCE(rp.name, ''),
            'hospitalName', COALESCE(rp.hospital, ''),
            'referralCode', COALESCE(rp.code, ''),
            'remarks', v.remarks,
            'amount', COALESCE(v.gross_amount, 0),
            'discountAmount', COALESCE(v.discount_amount, 0),
            'finalAmount', COALESCE(v.final_amount, v.gross_amount - v.discount_amount, 0),
            'investigations', inv.items, 'payments', pay.items
          ) ORDER BY v.visit_date DESC, v.id DESC
        ), '[]'::jsonb)::text
        FROM visits v
        JOIN patients p ON p.id = v.patient_id
        LEFT JOIN referral_parties rp ON rp.id = v.referral_party_id
        LEFT JOIN LATERAL (
          SELECT COALESCE(jsonb_agg(jsonb_build_object(
            'id', vi.id, 'investigationId', i.id,
            'code', i.code, 'name', i.name, 'modality', i.modality,
            'amount', vi.amount, 'concession', vi.concession,
            'discountAmount', vi.discount_amount,
            'discountReason', vi.discount_reason,
            'referralAmount', vi.referral_amount
          ) ORDER BY vi.id), '[]'::jsonb) AS items
          FROM visit_investigations vi
          JOIN investigations i ON i.id = vi.investigation_id
          WHERE vi.visit_id = v.id
        ) inv ON TRUE
        LEFT JOIN LATERAL (
          SELECT COALESCE(jsonb_agg(jsonb_build_object(
            'id', pm.id,
            'mode', CASE pm.mode WHEN 'UPI' THEN 'UPI' WHEN 'CARD' THEN 'Card' ELSE 'Cash' END,
            'amount', pm.amount, 'referenceNo', pm.reference_no
          ) ORDER BY pm.id), '[]'::jsonb) AS items
          FROM payments pm WHERE pm.visit_id = v.id
        ) pay ON TRUE
        WHERE p.patient_code = :patientCode
        """;

    public List<PatientDetailedReportResponse> findByPatientCode(String patientCode) {
        String json = jdbc.queryForObject(SQL,
            new MapSqlParameterSource("patientCode", patientCode), String.class);
        try {
            return mapper.readValue(json == null ? "[]" : json,
                new TypeReference<List<PatientDetailedReportResponse>>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse patient report query result", ex);
        }
    }
}
